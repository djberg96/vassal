package VASSAL.build.module.documentation.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RulesAnswerFormatterTest {
  @Test
  public void rendersBoldItalicAndBullets() {
    final String html = RulesAnswerFormatter.toHtml(
      "In *Hastings: 1066*, stacking is limited.\n\n"
        + "* **Combat Units:** One combat unit per hex.\n"
        + "* **Leaders:** Leaders are an exception."
    );

    assertTrue(html.contains("In <i>Hastings: 1066</i>, stacking is limited."));
    assertTrue(html.contains("<ul>"));
    assertTrue(html.contains("<li><b>Combat Units:</b> One combat unit per hex.</li>"));
    assertTrue(html.contains("<li><b>Leaders:</b> Leaders are an exception.</li>"));
  }

  @Test
  public void escapesHtml() {
    final String html = RulesAnswerFormatter.toHtml("<script>alert(\"nope\")</script>");

    assertTrue(html.contains("&lt;script&gt;alert(&quot;nope&quot;)&lt;/script&gt;"));
  }

  @Test
  public void rendersMarkdownTable() {
    final String html = RulesAnswerFormatter.toHtml(
      "| Rule | Result |\n"
        + "| --- | --- |\n"
        + "| **Stacking** | One unit |"
    );

    assertTrue(html.contains("<table>"));
    assertTrue(html.contains("<th>Rule</th>"));
    assertTrue(html.contains("<td><b>Stacking</b></td>"));
  }

  @Test
  public void emptyAnswerStillProducesHtmlDocument() {
    assertEquals(true, RulesAnswerFormatter.toHtml("").contains("<html>"));
  }
}
