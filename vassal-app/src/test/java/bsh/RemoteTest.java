package bsh;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RemoteTest {
  @TempDir
  private Path tempDir;
  private HttpServer server;

  @AfterEach
  public void stopServer() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  public void buildFormDataEncodesScriptAsUtf8() {
    final String script = "print(\"cafe \u00e9 \u96ea\");";
    final String prefix = "bsh.client=Remote&bsh.script=";
    final String formData = Remote.buildFormData(script);

    assertTrue(formData.startsWith(prefix));
    assertEquals(
      script,
      URLDecoder.decode(formData.substring(prefix.length()), StandardCharsets.UTF_8)
    );
  }

  @Test
  public void getFileReadsUtf8Text() throws IOException {
    final String script = "print(\"cafe \u00e9 \u96ea\");\n";
    final Path file = tempDir.resolve("script.bsh");
    Files.writeString(file, script, StandardCharsets.UTF_8);

    assertEquals(script, Remote.getFile(file.toString()));
  }

  @Test
  public void doHttpPostsScriptAndReadsReturnHeader() throws IOException {
    final String script = "print(\"hello\");";
    final AtomicReference<String> body = new AtomicReference<>();
    startServer(exchange -> {
      body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
      exchange.getResponseHeaders().set("Bsh-Return", "7");
      send(exchange, "done\n");
    });

    final String returnValue = Remote.doHttp(serverUrl(), script);

    assertEquals("7", returnValue);
    assertTrue(body.get().startsWith("bsh.client=Remote&bsh.script="));
    assertEquals(
      script,
      URLDecoder.decode(
        body.get().substring("bsh.client=Remote&bsh.script=".length()),
        StandardCharsets.UTF_8
      )
    );
  }

  private String serverUrl() {
    return "http://127.0.0.1:" + server.getAddress().getPort() + "/remote";
  }

  private void startServer(ExchangeHandler handler) throws IOException {
    server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    server.createContext("/", exchange -> {
      try {
        handler.handle(exchange);
      }
      finally {
        exchange.close();
      }
    });
    server.start();
  }

  private static void send(HttpExchange exchange, String body) throws IOException {
    final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(200, bytes.length);
    try (OutputStream out = exchange.getResponseBody()) {
      out.write(bytes);
    }
  }

  @FunctionalInterface
  private interface ExchangeHandler {
    void handle(HttpExchange exchange) throws IOException;
  }
}
