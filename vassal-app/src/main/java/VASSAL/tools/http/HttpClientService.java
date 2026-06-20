/*
 * Copyright (c) 2026 by the Vassal developers
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 */
package VASSAL.tools.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class HttpClientService {
  private final HttpClient client;
  private final Duration requestTimeout;

  public static HttpClientService createDefault(Duration requestTimeout) {
    return new HttpClientService(
      HttpClient.newBuilder()
        .connectTimeout(requestTimeout)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build(),
      requestTimeout
    );
  }

  public HttpClientService(HttpClient client, Duration requestTimeout) {
    this.client = Objects.requireNonNull(client, "client"); //NON-NLS
    this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout"); //NON-NLS
  }

  public HttpResponseData getJson(URI uri) throws IOException {
    return send(
      requestBuilder(uri)
        .GET()
        .header("Accept", "application/json") //NON-NLS
        .build()
    );
  }

  public HttpResponseData postJson(URI uri, String body) throws IOException {
    return postJson(uri, body, Map.of());
  }

  public HttpResponseData postJson(URI uri, String body, Map<String, String> headers)
      throws IOException {
    final HttpRequest.Builder builder = requestBuilder(uri)
      .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
      .header("Accept", "application/json") //NON-NLS
      .header("Content-Type", "application/json; charset=UTF-8"); //NON-NLS

    headers.forEach(builder::header);
    return send(builder.build());
  }

  public HttpResponseData send(HttpRequest request) throws IOException {
    try {
      final HttpResponse<String> response = client.send(
        request,
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
      );
      return new HttpResponseData(response.statusCode(), response.body());
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while waiting for HTTP response.", e); //NON-NLS
    }
  }

  private HttpRequest.Builder requestBuilder(URI uri) {
    return HttpRequest.newBuilder(Objects.requireNonNull(uri, "uri")) //NON-NLS
      .timeout(requestTimeout);
  }
}
