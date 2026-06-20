package VASSAL.build.module.documentation.ai;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class RulesAssistantServiceTest {
  @Test
  public void buildPromptIncludesCitationsAndQuestion() {
    final String prompt = RulesAssistantService.buildPrompt(
      "Can archers fire?",
      List.of(new RulesChunk("Rules", "rules.pdf", 3, "Archers may fire during the missile phase."))
    );

    assertTrue(prompt.contains("Can archers fire?"));
    assertTrue(prompt.contains("[Rules, p. 3]"));
    assertTrue(prompt.contains("Archers may fire during the missile phase."));
  }
}
