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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ManusRulesAssistantClient implements RulesAssistantClient {
  private static final int HTTP_TIMEOUT_MS = 60_000;
  private static final int POLL_INTERVAL_MS = 1_500;
  private static final int MAX_POLL_ATTEMPTS = 60;
  private static final int MAX_NOT_FOUND_RETRIES = 8;
  static final String JSON_CONTENT_TYPE = "application/json"; //NON-NLS

  private final String apiKey;
  private final String agentProfile;
  private final String baseUrl;
  private final String taskTitle;
  private String taskId;
  private int assistantMessagesSeen;

  public ManusRulesAssistantClient(String apiKey, String agentProfile, String baseUrl, String taskTitle) {
    this.apiKey = apiKey;
    this.agentProfile = agentProfile;
    this.baseUrl = baseUrl;
    this.taskTitle = taskTitle;
  }

  @Override
  public String answer(String prompt) throws IOException {
    if (apiKey == null || apiKey.isBlank()) {
      throw new IOException("Manus API key is required. Set it in Preferences > Rules Assistant."); //NON-NLS
    }

    if (taskId == null) {
      taskId = createTask(prompt);
    }
    else {
      sendMessage(prompt);
    }

    final Answer answer = waitForAnswer(taskId, assistantMessagesSeen);
    assistantMessagesSeen = answer.assistantMessageCount();
    return answer.content();
  }

  String createTaskJson(String prompt) {
    return "{"
      + "\"message\":{\"content\":" + OpenAIRulesAssistantClient.jsonString(prompt) + "},"
      + "\"agent_profile\":" + OpenAIRulesAssistantClient.jsonString(agentProfile) + ','
      + "\"interactive_mode\":false,"
      + "\"share_visibility\":\"private\","
      + "\"title\":" + OpenAIRulesAssistantClient.jsonString(taskTitle)
      + "}";
  }

  String sendMessageJson(String prompt) {
    return "{"
      + "\"task_id\":" + OpenAIRulesAssistantClient.jsonString(taskId) + ','
      + "\"message\":{\"content\":" + OpenAIRulesAssistantClient.jsonString(prompt) + "},"
      + "\"agent_profile\":" + OpenAIRulesAssistantClient.jsonString(agentProfile)
      + "}";
  }

  static String latestAssistantContent(String response) throws IOException {
    String content = null;
    int index = response.indexOf("\"assistant_message\""); //NON-NLS
    while (index >= 0) {
      final String candidate = OpenAIRulesAssistantClient.jsonStringProperty(response.substring(index), "content"); //NON-NLS
      if (candidate != null && !candidate.isBlank()) {
        content = candidate;
      }
      index = response.indexOf("\"assistant_message\"", index + 1); //NON-NLS
    }
    return content;
  }

  static int assistantMessageCount(String response) {
    int count = 0;
    int index = response.indexOf("\"type\""); //NON-NLS
    while (index >= 0) {
      try {
        if ("assistant_message".equals(OpenAIRulesAssistantClient.jsonStringProperty(response.substring(index), "type"))) { //NON-NLS
          count++;
        }
      }
      catch (IOException e) {
        return count;
      }
      index = response.indexOf("\"type\"", index + 1); //NON-NLS
    }
    return count;
  }

  static String latestAgentStatus(String response) throws IOException {
    String status = null;
    int index = response.indexOf("\"agent_status\""); //NON-NLS
    while (index >= 0) {
      final String candidate = OpenAIRulesAssistantClient.jsonStringProperty(response.substring(index), "agent_status"); //NON-NLS
      if (candidate != null && !candidate.isBlank()) {
        status = candidate;
      }
      index = response.indexOf("\"agent_status\"", index + 1); //NON-NLS
    }
    return status;
  }

  static String firstErrorMessage(String response) throws IOException {
    int index = response.indexOf("\"error_message\""); //NON-NLS
    if (index >= 0) {
      return OpenAIRulesAssistantClient.jsonStringProperty(response.substring(index), "content"); //NON-NLS
    }

    index = response.indexOf("\"error\""); //NON-NLS
    if (index >= 0) {
      return OpenAIRulesAssistantClient.jsonStringProperty(response.substring(index), "message"); //NON-NLS
    }
    return null;
  }

  static boolean isTaskNotFound(String response) throws IOException {
    return OpenAIRulesAssistantClient.hasJsonStringProperty(response, "code", "not_found") //NON-NLS
      && "task not found".equalsIgnoreCase(firstErrorMessage(response)); //NON-NLS
  }

  private String createTask(String prompt) throws IOException {
    final HttpURLConnection connection = openConnection("/v2/task.create"); //NON-NLS
    connection.setDoOutput(true);
    connection.setRequestMethod("POST"); //NON-NLS
    connection.setRequestProperty("Content-Type", JSON_CONTENT_TYPE); //NON-NLS

    try (OutputStream out = connection.getOutputStream()) {
      out.write(createTaskJson(prompt).getBytes(StandardCharsets.UTF_8));
    }

    final Response response = readResponse(connection);
    if (response.status() >= 400) {
      throw new IOException("Manus task.create returned HTTP " + response.status() + ": " + response.body()); //NON-NLS
    }

    final String error = firstErrorMessage(response.body());
    if (error != null && !error.isBlank()) {
      throw new IOException("Manus returned an error: " + error); //NON-NLS
    }

    final String taskId = OpenAIRulesAssistantClient.jsonStringProperty(response.body(), "task_id"); //NON-NLS
    if (taskId == null || taskId.isBlank()) {
      throw new IOException("Manus response did not contain a task_id."); //NON-NLS
    }
    return taskId;
  }

  private void sendMessage(String prompt) throws IOException {
    final HttpURLConnection connection = openConnection("/v2/task.sendMessage"); //NON-NLS
    connection.setDoOutput(true);
    connection.setRequestMethod("POST"); //NON-NLS
    connection.setRequestProperty("Content-Type", JSON_CONTENT_TYPE); //NON-NLS

    try (OutputStream out = connection.getOutputStream()) {
      out.write(sendMessageJson(prompt).getBytes(StandardCharsets.UTF_8));
    }

    final Response response = readResponse(connection);
    if (response.status() >= 400) {
      throw new IOException("Manus task.sendMessage returned HTTP " + response.status()
        + " for task " + taskId + ": " + response.body()); //NON-NLS
    }

    final String error = firstErrorMessage(response.body());
    if (error != null && !error.isBlank()) {
      throw new IOException("Manus returned an error: " + error); //NON-NLS
    }
  }

  private Answer waitForAnswer(String taskId, int previousAssistantMessages) throws IOException {
    String lastAnswer = null;
    int seenAssistantMessages = previousAssistantMessages;
    int notFoundRetries = 0;
    for (int attempt = 0; attempt < MAX_POLL_ATTEMPTS; attempt++) {
      final Response listResponse = listMessages(taskId);
      final String response = listResponse.body();
      if (listResponse.status() == 404 && isTaskNotFound(response) && notFoundRetries++ < MAX_NOT_FOUND_RETRIES) {
        sleepBeforeRetry();
        continue;
      }
      if (listResponse.status() >= 400) {
        throw new IOException("Manus task.listMessages returned HTTP " + listResponse.status()
          + " for task " + taskId + ": " + response); //NON-NLS
      }

      final String error = firstErrorMessage(response);
      if (error != null && !error.isBlank()) {
        throw new IOException("Manus task failed: " + error); //NON-NLS
      }

      seenAssistantMessages = assistantMessageCount(response);
      if (seenAssistantMessages > previousAssistantMessages) {
        final String answer = latestAssistantContent(response);
        if (answer != null && !answer.isBlank()) {
          lastAnswer = answer;
        }
      }

      final String status = latestAgentStatus(response);
      if ("stopped".equals(status) && seenAssistantMessages > previousAssistantMessages) { //NON-NLS
        if (lastAnswer != null && !lastAnswer.isBlank()) {
          return new Answer(lastAnswer.strip(), seenAssistantMessages);
        }
        throw new IOException("Manus task finished without an assistant response."); //NON-NLS
      }

      if ("waiting".equals(status)) { //NON-NLS
        throw new IOException("Manus needs follow-up input, which the Rules Assistant does not support yet."); //NON-NLS
      }

      if ("error".equals(status)) { //NON-NLS
        throw new IOException("Manus task entered an error state."); //NON-NLS
      }

      sleepBeforeRetry();
    }
    throw new IOException("Timed out waiting for Manus to answer."); //NON-NLS
  }

  private Response listMessages(String taskId) throws IOException {
    final String query = "?task_id=" + URLEncoder.encode(taskId, StandardCharsets.UTF_8)
      + "&order=asc&limit=50"; //NON-NLS
    final HttpURLConnection connection = openConnection("/v2/task.listMessages" + query); //NON-NLS
    connection.setRequestMethod("GET"); //NON-NLS
    return readResponse(connection);
  }

  private HttpURLConnection openConnection(String path) throws IOException {
    final HttpURLConnection connection =
      (HttpURLConnection) URI.create(baseUrl.strip().replaceAll("/+$", "") + path).toURL().openConnection(); //NON-NLS
    connection.setConnectTimeout(HTTP_TIMEOUT_MS);
    connection.setReadTimeout(HTTP_TIMEOUT_MS);
    connection.setRequestProperty("Accept", "application/json"); //NON-NLS
    connection.setRequestProperty("x-manus-api-key", apiKey); //NON-NLS
    return connection;
  }

  private static Response readResponse(HttpURLConnection connection) throws IOException {
    final int status = connection.getResponseCode();
    final InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
    final String body = stream == null ? "" : new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    return new Response(status, body);
  }

  private static void sleepBeforeRetry() throws IOException {
    try {
      Thread.sleep(POLL_INTERVAL_MS);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while waiting for Manus to answer.", e); //NON-NLS
    }
  }

  private record Response(int status, String body) {
  }

  private record Answer(String content, int assistantMessageCount) {
  }
}
