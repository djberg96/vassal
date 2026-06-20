/*
 * Copyright (c) 2026 by the Vassal developers
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 */
package VASSAL.tools.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class HttpClientServiceTest {
  private HttpServer server;

  @AfterEach
  void stopServer() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void getJsonReadsResponseBodyAndSendsAcceptHeader() throws IOException {
    final AtomicReference<List<String>> acceptHeaders = new AtomicReference<>();
    startServer(exchange -> {
      acceptHeaders.set(exchange.getRequestHeaders().get("Accept")); //NON-NLS
      send(exchange, 200, "{\"ok\":true}"); //NON-NLS
    });

    final HttpResponseData response = client().getJson(uri("/status")); //NON-NLS

    assertEquals(200, response.status());
    assertEquals("{\"ok\":true}", response.body()); //NON-NLS
    assertTrue(acceptHeaders.get().contains("application/json")); //NON-NLS
  }

  @Test
  void postJsonSendsBodyAndHeaders() throws IOException {
    final AtomicReference<String> method = new AtomicReference<>();
    final AtomicReference<String> body = new AtomicReference<>();
    final AtomicReference<List<String>> authHeaders = new AtomicReference<>();
    startServer(exchange -> {
      method.set(exchange.getRequestMethod());
      body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
      authHeaders.set(exchange.getRequestHeaders().get("Authorization")); //NON-NLS
      send(exchange, 201, "{\"created\":true}"); //NON-NLS
    });

    final HttpResponseData response = client().postJson(
      uri("/create"), //NON-NLS
      "{\"name\":\"counter\"}", //NON-NLS
      Map.of("Authorization", "Bearer key") //NON-NLS
    );

    assertEquals(201, response.status());
    assertEquals("{\"created\":true}", response.body()); //NON-NLS
    assertEquals("POST", method.get()); //NON-NLS
    assertEquals("{\"name\":\"counter\"}", body.get()); //NON-NLS
    assertTrue(authHeaders.get().contains("Bearer key")); //NON-NLS
  }

  @Test
  void requireSuccessIncludesErrorBody() {
    final HttpResponseData response = new HttpResponseData(503, "not today"); //NON-NLS

    final IOException e = assertThrows(
      IOException.class,
      () -> response.requireSuccess("dice.example") //NON-NLS
    );

    assertEquals("dice.example returned HTTP 503: not today", e.getMessage()); //NON-NLS
  }

  private HttpClientService client() {
    return HttpClientService.createDefault(Duration.ofSeconds(5));
  }

  private URI uri(String path) {
    return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + path); //NON-NLS
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
