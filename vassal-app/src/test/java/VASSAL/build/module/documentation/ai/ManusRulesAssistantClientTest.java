package VASSAL.build.module.documentation.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class ManusRulesAssistantClientTest {
  @Test
  public void createTaskJsonUsesHiddenPrivateLiteTask() {
    final ManusRulesAssistantClient client = new ManusRulesAssistantClient(
      "key",
      RulesAssistantPrefs.DEFAULT_MANUS_MODEL,
      RulesAssistantPrefs.MANUS_BASE_URL,
      "VASSAL Rules Assistant - Hastings 1066"
    );

    final String json = client.createTaskJson("Question?");

    assertEquals(true, json.contains("\"message\":{\"content\":\"Question?\"}"));
    assertEquals(true, json.contains("\"agent_profile\":\"manus-1.6-lite\""));
    assertEquals(true, json.contains("\"interactive_mode\":false"));
    assertEquals(true, json.contains("\"share_visibility\":\"private\""));
    assertEquals(true, json.contains("\"title\":\"VASSAL Rules Assistant - Hastings 1066\""));
  }

  @Test
  public void latestAssistantContentReturnsMostRecentAnswer() throws IOException {
    final String response = "{"
      + "\"messages\":["
      + "{\"type\":\"assistant_message\",\"assistant_message\":{\"content\":\"First answer\"}},"
      + "{\"type\":\"status_update\",\"status_update\":{\"agent_status\":\"running\"}},"
      + "{\"type\":\"assistant_message\",\"assistant_message\":{\"content\":\"Final answer\"}}"
      + "]"
      + "}";

    assertEquals("Final answer", ManusRulesAssistantClient.latestAssistantContent(response));
  }

  @Test
  public void assistantMessageCountCountsExistingAnswers() {
    final String response = "{"
      + "\"messages\":["
      + "{\"type\":\"assistant_message\",\"assistant_message\":{\"content\":\"First answer\"}},"
      + "{\"type\":\"assistant_message\",\"assistant_message\":{\"content\":\"Final answer\"}}"
      + "]"
      + "}";

    assertEquals(2, ManusRulesAssistantClient.assistantMessageCount(response));
  }

  @Test
  public void latestAgentStatusReturnsMostRecentStatus() throws IOException {
    final String response = "{"
      + "\"messages\":["
      + "{\"type\":\"status_update\",\"status_update\":{\"agent_status\":\"stopped\"}},"
      + "{\"type\":\"status_update\",\"status_update\":{\"agent_status\":\"running\"}}"
      + "]"
      + "}";

    assertEquals("running", ManusRulesAssistantClient.latestAgentStatus(response));
  }

  @Test
  public void firstErrorMessageReadsTaskError() throws IOException {
    final String response = "{"
      + "\"messages\":["
      + "{\"type\":\"error_message\",\"error_message\":{\"content\":\"Tool failed\"}}"
      + "]"
      + "}";

    assertEquals("Tool failed", ManusRulesAssistantClient.firstErrorMessage(response));
  }

  @Test
  public void firstErrorMessageReadsWrapperError() throws IOException {
    final String response = "{\"ok\":false,\"error\":{\"code\":\"permission_denied\",\"message\":\"Bad key\"}}";

    assertEquals("Bad key", ManusRulesAssistantClient.firstErrorMessage(response));
  }

  @Test
  public void isTaskNotFoundRecognizesTransientPollError() throws IOException {
    final String response = "{\"ok\":false,\"error\":{\"code\":\"not_found\",\"message\":\"task not found\"}}";

    assertEquals(true, ManusRulesAssistantClient.isTaskNotFound(response));
  }
}
