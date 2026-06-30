package VASSAL.build.module.documentation.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
  public void jsonStringPropertyRejectsUnterminatedString() {
    final String json = "{\"output_text\":\"Line 1}";

    assertThrows(IOException.class, () -> OpenAIRulesAssistantClient.jsonStringProperty(json, "output_text"));
  }

  @Test
  public void jsonStringPropertyRejectsInvalidUnicodeEscape() {
    final String json = "{\"output_text\":\"Bad \\u12xz\"}";

    assertThrows(IOException.class, () -> OpenAIRulesAssistantClient.jsonStringProperty(json, "output_text"));
  }

  @Test
  public void responseOutputTextReadsNestedResponsesText() throws IOException {
    final String json = "{"
      + "\"output\":["
      + "{\"type\":\"message\",\"content\":["
      + "{\"type\":\"output_text\",\"text\":\"Final answer\"}"
      + "]}"
      + "]"
      + "}";

    assertEquals("Final answer", OpenAIRulesAssistantClient.responseOutputText(json));
  }

  @Test
  public void responseOutputTextIgnoresReasoningSummaryText() throws IOException {
    final String json = "{"
      + "\"output\":["
      + "{\"type\":\"reasoning\",\"summary\":[{\"type\":\"summary_text\",\"text\":\"Internal scratchpad\"}]},"
      + "{\"type\":\"message\",\"content\":["
      + "{\"type\":\"output_text\",\"text\":\"Player-facing answer\"}"
      + "]}"
      + "]"
      + "}";

    assertEquals("Player-facing answer", OpenAIRulesAssistantClient.responseOutputText(json));
  }

  @Test
  public void responseOutputTextReturnsNullWhenTextIsMissing() throws IOException {
    final String json = "{\"output\":[{\"type\":\"message\",\"content\":[]}]}";

    assertNull(OpenAIRulesAssistantClient.responseOutputText(json));
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
