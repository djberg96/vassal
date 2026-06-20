package VASSAL.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class HttpRequestWrapperTest {
  private HttpServer server;

  @AfterEach
  void stopServer() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void doGetSendsQueryParametersAndReturnsLines() throws IOException {
    final AtomicReference<String> query = new AtomicReference<>();
    startServer(exchange -> {
      query.set(exchange.getRequestURI().getRawQuery());
      send(exchange, 200, "alpha\nbeta"); //NON-NLS
    });

    final Properties props = new Properties();
    props.setProperty("module", "Hastings 1066"); //NON-NLS

    final List<String> lines = wrapper().doGet("status", props); //NON-NLS

    assertEquals(List.of("alpha", "beta"), lines); //NON-NLS
    assertEquals("module=Hastings+1066", query.get()); //NON-NLS
  }

  @Test
  void doPostSendsUrlEncodedFormAndReturnsLines() throws IOException {
    final AtomicReference<String> method = new AtomicReference<>();
    final AtomicReference<String> body = new AtomicReference<>();
    startServer(exchange -> {
      method.set(exchange.getRequestMethod());
      body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
      send(exchange, 201, "created\nok"); //NON-NLS
    });

    final Properties props = new Properties();
    props.setProperty("content", "hello world"); //NON-NLS

    final List<String> lines = wrapper().doPost("post", props); //NON-NLS

    assertEquals("POST", method.get()); //NON-NLS
    assertEquals("content=hello+world", body.get()); //NON-NLS
    assertEquals(List.of("created", "ok"), lines); //NON-NLS
  }

  @Test
  void doGetIncludesErrorBodyWhenStatusUnexpected() throws IOException {
    startServer(exchange -> send(exchange, 503, "offline")); //NON-NLS

    final IOException e = assertThrows(
      IOException.class,
      () -> wrapper().doGet("status", new Properties()) //NON-NLS
    );

    assertTrue(e.getMessage().contains("Failed to GET")); //NON-NLS
    assertTrue(e.getCause().getMessage().contains("503: offline")); //NON-NLS
  }

  private HttpRequestWrapper wrapper() {
    return new HttpRequestWrapper("http://127.0.0.1:" + server.getAddress().getPort() + '/'); //NON-NLS
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
