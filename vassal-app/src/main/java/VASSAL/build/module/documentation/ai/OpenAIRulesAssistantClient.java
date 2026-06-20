/*
 * Copyright (c) 2026 by the Vassal developers
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 */
package VASSAL.build.module.documentation.ai;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class OpenAIRulesAssistantClient implements RulesAssistantClient {
  private static final int HTTP_TIMEOUT_MS = 60_000;
  private static final String SYSTEM_PROMPT =
    "You are a board wargame rules assistant. Answer only from the supplied rules excerpts. " //NON-NLS
      + "If the excerpts do not contain the answer, say that the rules excerpts do not answer the question. "
      + "Cite the provided source and page labels.";

  private final String apiKey;
  private final String model;
  private final String baseUrl;
  private final String providerName;

  public OpenAIRulesAssistantClient(String apiKey, String model, String baseUrl, String providerName) {
    this.apiKey = apiKey;
    this.model = model;
    this.baseUrl = baseUrl;
    this.providerName = providerName;
  }

  @Override
  public String answer(String prompt) throws IOException {
    if (apiKey == null || apiKey.isBlank()) {
      throw new IOException(providerName + " API key is required. Set it in Preferences > Rules Assistant."); //NON-NLS
    }

    final HttpURLConnection connection = (HttpURLConnection) URI.create(responsesUrl()).toURL().openConnection();
    connection.setConnectTimeout(HTTP_TIMEOUT_MS);
    connection.setReadTimeout(HTTP_TIMEOUT_MS);
    connection.setDoOutput(true);
    connection.setRequestMethod("POST"); //NON-NLS
    connection.setRequestProperty("Accept", "application/json"); //NON-NLS
    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8"); //NON-NLS
    connection.setRequestProperty("Authorization", "Bearer " + apiKey); //NON-NLS

    try (OutputStream out = connection.getOutputStream()) {
      out.write(requestJson(prompt).getBytes(StandardCharsets.UTF_8));
    }

    final String response = readConnectionResponse(connection);
    final String output = jsonStringProperty(response, "output_text"); //NON-NLS
    if (output != null && !output.isBlank()) {
      return output.strip();
    }

    final String text = jsonStringProperty(response, "text"); //NON-NLS
    if (text != null && !text.isBlank()) {
      return text.strip();
    }

    throw new IOException("OpenAI response did not contain text output."); //NON-NLS
  }

  String requestJson(String prompt) {
    return "{"
      + "\"model\":" + jsonString(model) + ','
      + "\"input\":["
      + "{\"role\":\"system\",\"content\":" + jsonString(SYSTEM_PROMPT) + "},"
      + "{\"role\":\"user\",\"content\":" + jsonString(prompt) + "}"
      + "]"
      + "}";
  }

  private String responsesUrl() {
    return baseUrl.strip().replaceAll("/+$", "") + "/responses"; //NON-NLS
  }

  static String readConnectionResponse(HttpURLConnection connection) throws IOException {
    final int status = connection.getResponseCode();
    final InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
    final String response = stream == null ? "" : new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    if (status >= 400) {
      throw new IOException("AI provider returned HTTP " + status + ": " + response); //NON-NLS
    }
    return response;
  }

  static boolean hasJsonStringProperty(String json, String propertyName, String expectedValue) throws IOException {
    final String needle = '"' + propertyName + '"';
    int index = json.indexOf(needle);
    while (index >= 0) {
      int colon = index + needle.length();
      while (colon < json.length() && Character.isWhitespace(json.charAt(colon))) {
        colon++;
      }
      if (colon < json.length() && json.charAt(colon) == ':') {
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
          start++;
        }
        if (start < json.length() && json.charAt(start) == '"') {
          if (expectedValue.equals(readJsonString(json, start))) {
            return true;
          }
        }
      }
      index = json.indexOf(needle, index + needle.length());
    }
    return false;
  }

  static String jsonString(String value) {
    if (value == null) {
      return "null"; //NON-NLS
    }

    final StringBuilder sb = new StringBuilder(value.length() + 2);
    sb.append('"');
    for (int i = 0; i < value.length(); i++) {
      final char c = value.charAt(i);
      switch (c) {
      case '"' -> sb.append("\\\"");
      case '\\' -> sb.append("\\\\");
      case '\b' -> sb.append("\\b");
      case '\f' -> sb.append("\\f");
      case '\n' -> sb.append("\\n");
      case '\r' -> sb.append("\\r");
      case '\t' -> sb.append("\\t");
      default -> {
        if (c < 0x20) {
          sb.append(String.format("\\u%04x", (int) c)); //NON-NLS
        }
        else {
          sb.append(c);
        }
      }
      }
    }
    sb.append('"');
    return sb.toString();
  }

  static String jsonStringProperty(String json, String propertyName) throws IOException {
    final String needle = '"' + propertyName + '"';
    int index = json.indexOf(needle);
    while (index >= 0) {
      int colon = index + needle.length();
      while (colon < json.length() && Character.isWhitespace(json.charAt(colon))) {
        colon++;
      }
      if (colon < json.length() && json.charAt(colon) == ':') {
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
          start++;
        }
        if (start < json.length() && json.charAt(start) == '"') {
          return readJsonString(json, start);
        }
      }
      index = json.indexOf(needle, index + needle.length());
    }
    return null;
  }

  private static String readJsonString(String json, int quoteIndex) throws IOException {
    final StringBuilder sb = new StringBuilder();
    for (int i = quoteIndex + 1; i < json.length(); i++) {
      final char c = json.charAt(i);
      if (c == '"') {
        return sb.toString();
      }
      if (c != '\\') {
        sb.append(c);
        continue;
      }
      if (++i >= json.length()) {
        throw new IOException("Unterminated JSON escape sequence."); //NON-NLS
      }
      final char escape = json.charAt(i);
      switch (escape) {
      case '"' -> sb.append('"');
      case '\\' -> sb.append('\\');
      case '/' -> sb.append('/');
      case 'b' -> sb.append('\b');
      case 'f' -> sb.append('\f');
      case 'n' -> sb.append('\n');
      case 'r' -> sb.append('\r');
      case 't' -> sb.append('\t');
      case 'u' -> {
        if (i + 4 >= json.length()) {
          throw new IOException("Incomplete JSON unicode escape."); //NON-NLS
        }
        sb.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
        i += 4;
      }
      default -> throw new IOException("Unsupported JSON escape: " + escape); //NON-NLS
      }
    }
    throw new IOException("Unterminated JSON string."); //NON-NLS
  }
}
