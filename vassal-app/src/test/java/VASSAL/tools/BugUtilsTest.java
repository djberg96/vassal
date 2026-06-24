package VASSAL.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class BugUtilsTest {
  private static final String ERROR_LOG = """
    2026-06-20 INFO Starting
    2026-06-20 ERROR VASSAL.tools.ErrorDialog -
    java.lang.IllegalStateException: boom
    \tat test.Stack.one(Stack.java:1)
    \tat test.Stack.two(Stack.java:2)
    """;

  private HttpServer server;

  @AfterEach
  void stopServer() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void buildBugReportBodyContainsExpectedParts() throws IOException {
    final BugUtils.MultipartBody body = BugUtils.buildBugReportBody(
      "player@example.com", //NON-NLS
      "It broke", //NON-NLS
      ERROR_LOG,
      new IllegalStateException("boom") //NON-NLS
    );

    final String multipart = new String(body.body(), StandardCharsets.UTF_8);

    assertTrue(body.contentType().startsWith("multipart/form-data; boundary=")); //NON-NLS
    assertTrue(multipart.contains("name=\"email\"")); //NON-NLS
    assertTrue(multipart.contains("player@example.com")); //NON-NLS
    assertTrue(multipart.contains("name=\"summary\"")); //NON-NLS
    assertTrue(multipart.contains("IllegalStateException: boom")); //NON-NLS
    assertTrue(multipart.contains("name=\"log\"")); //NON-NLS
    assertTrue(multipart.contains("java.lang.IllegalStateException: boom")); //NON-NLS
  }

  @Test
  void sendBugReportPostsMultipartBody() throws IOException {
    final AtomicReference<List<String>> contentTypes = new AtomicReference<>();
    final AtomicReference<String> requestBody = new AtomicReference<>();
    startServer(exchange -> {
      contentTypes.set(exchange.getRequestHeaders().get("Content-Type")); //NON-NLS
      requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
      send(exchange, 201, "created"); //NON-NLS
    });

    BugUtils.sendBugReport(
      "player@example.com", //NON-NLS
      "It broke", //NON-NLS
      ERROR_LOG,
      null,
      serverUri()
    );

    assertTrue(contentTypes.get().get(0).startsWith("multipart/form-data; boundary=")); //NON-NLS
    assertTrue(requestBody.get().contains("name=\"description\"")); //NON-NLS
    assertTrue(requestBody.get().contains("It broke")); //NON-NLS
  }

  @Test
  void sendBugReportAcceptsOkStatus() throws IOException {
    startServer(exchange -> send(exchange, 200, "created")); //NON-NLS

    BugUtils.sendBugReport(
      "player@example.com", //NON-NLS
      "It broke", //NON-NLS
      ERROR_LOG,
      null,
      serverUri()
    );
  }

  @Test
  void sendBugReportThrowsOnUnexpectedStatus() throws IOException {
    startServer(exchange -> send(exchange, 503, "offline")); //NON-NLS

    try {
      BugUtils.sendBugReport("player@example.com", "It broke", ERROR_LOG, null, serverUri()); //NON-NLS
    }
    catch (IOException e) {
      assertEquals("Bug report failed: 503: offline", e.getMessage()); //NON-NLS
      return;
    }

    throw new AssertionError("Expected bug report upload failure"); //NON-NLS
  }

  private java.net.URI serverUri() {
    return java.net.URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/abr"); //NON-NLS
  }

  private void startServer(ExchangeHandler handler) throws IOException {
    server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    server.createContext("/", exchange -> { //NON-NLS
      try {
        handler.handle(exchange);
      }
      finally {
        exchange.close();
      }
    });
    server.start();
  }

  private static void send(HttpExchange exchange, int status, String body) throws IOException {
    final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(status, bytes.length);
    try (OutputStream out = exchange.getResponseBody()) {
      out.write(bytes);
    }
  }

  @FunctionalInterface
  private interface ExchangeHandler {
    void handle(HttpExchange exchange) throws IOException;
  }
}
