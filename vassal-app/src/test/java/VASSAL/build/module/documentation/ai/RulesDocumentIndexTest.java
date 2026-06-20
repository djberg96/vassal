package VASSAL.build.module.documentation.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class RulesDocumentIndexTest {
  @Test
  public void relevantChunksPreferQuestionTerms() {
    final RulesDocumentIndex index = new RulesDocumentIndex(List.of(
      new RulesChunk("Rules", "rules.pdf", 1, "Archers may fire before movement."),
      new RulesChunk("Rules", "rules.pdf", 2, "Cavalry may charge downhill.")
    ));

    final List<RulesChunk> chunks = index.relevantChunks("When may cavalry charge?");

    assertEquals(2, chunks.get(0).page());
  }

  @Test
  public void tokenizeDropsTinyWords() {
    assertTrue(RulesDocumentIndex.tokenize("to be or infantry").contains("infantry"));
    assertEquals(1, RulesDocumentIndex.tokenize("to be or infantry").size());
  }
}
