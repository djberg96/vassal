package VASSAL.build.module.documentation.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class OpenAIRulesAssistantClientTest {
  @Test
  public void jsonStringEscapesControlCharacters() {
    assertEquals("\"a\\n\\\"b\\\"\\\\c\"", OpenAIRulesAssistantClient.jsonString("a\n\"b\"\\c"));
  }

  @Test
  public void jsonStringPropertyReadsEscapedText() throws IOException {
    final String json = "{\"output_text\":\"Line 1\\nLine \\\"2\\\"\"}";

    assertEquals("Line 1\nLine \"2\"", OpenAIRulesAssistantClient.jsonStringProperty(json, "output_text"));
  }

  @Test
  public void hasJsonStringPropertyFindsLaterMatchingValue() throws IOException {
    final String json = "{\"agent_status\":\"running\"},{\"agent_status\":\"stopped\"}";

    assertEquals(true, OpenAIRulesAssistantClient.hasJsonStringProperty(json, "agent_status", "stopped"));
  }

  @Test
  public void requestUsesResponsesMessageArray() {
    final OpenAIRulesAssistantClient client = new OpenAIRulesAssistantClient(
      "key",
      "grok-4.3",
      RulesAssistantPrefs.XAI_BASE_URL,
      RulesAssistantPrefs.XAI_PROVIDER
    );

    final String json = client.requestJson("Question?");

    assertEquals(true, json.contains("\"model\":\"grok-4.3\""));
    assertEquals(true, json.contains("\"role\":\"system\""));
    assertEquals(true, json.contains("\"role\":\"user\""));
    assertEquals(true, json.contains("\"content\":\"Question?\""));
  }
}
