package VASSAL.build;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class BuilderTest {
  @Test
  void toStringSerializesXmlDocument() {
    final Document doc = Builder.createNewDocument();
    final Element root = doc.createElement("root");
    root.setAttribute("name", "A&B");
    root.appendChild(doc.createTextNode("4 > 2"));
    doc.appendChild(root);

    final String xml = Builder.toString(doc);

    assertTrue(xml.contains("<root name=\"A&amp;B\">"));
    assertTrue(xml.contains("4 &gt; 2"));
  }
}
