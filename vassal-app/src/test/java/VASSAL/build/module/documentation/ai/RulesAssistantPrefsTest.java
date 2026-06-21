package VASSAL.build.module.documentation.ai;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class RulesAssistantPrefsTest {
  @Test
  public void modelIdsFromModelsResponseReadsIdsInOrder() throws IOException {
    final String response = "{"
      + "\"object\":\"list\","
      + "\"data\":["
      + "{\"id\":\"gpt-5-mini\",\"object\":\"model\"},"
      + "{\"id\":\"gpt-5.5\",\"object\":\"model\"}"
      + "]"
      + "}";

    assertArrayEquals(
      new String[] {"gpt-5-mini", "gpt-5.5"},
      RulesAssistantPrefs.modelIdsFromModelsResponse(response)
    );
  }

  @Test
  public void modelIdsFromModelsResponseDeduplicatesIds() throws IOException {
    final String response = "{"
      + "\"data\":["
      + "{\"id\":\"grok-4.3\"},"
      + "{\"id\":\"grok-4.3\"},"
      + "{\"id\":\"grok-build-0.1\"}"
      + "]"
      + "}";

    assertArrayEquals(
      new String[] {"grok-4.3", "grok-build-0.1"},
      RulesAssistantPrefs.modelIdsFromModelsResponse(response)
    );
  }
}
