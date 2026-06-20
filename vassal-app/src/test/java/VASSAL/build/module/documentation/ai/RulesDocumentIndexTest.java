package VASSAL.build.module.documentation.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import VASSAL.build.Widget;
import VASSAL.build.widget.Chart;

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

  @Test
  public void chartCitationUsesChartLabel() {
    final RulesChunk chunk = new RulesChunk("Chart: Terrain Effects", "terrain.png", 0, "Image file: terrain.png");

    assertEquals("Chart: Terrain Effects (terrain.png)", chunk.citation());
  }

  @Test
  public void imageChartMetadataIsSearchableText() {
    final Chart chart = new Chart();
    chart.setConfigureName("Combat Results Table");
    chart.setAttribute(Widget.DESCRIPTION, "Cross reference attack strength and defense strength.");
    chart.setAttribute(Chart.FILE, "crt.png");

    final String text = RulesDocumentIndex.chartText(chart);

    assertTrue(text.contains("Combat Results Table"));
    assertTrue(text.contains("attack strength"));
    assertTrue(text.contains("crt.png"));
    assertTrue(text.contains("not available as searchable text yet"));
  }
}
