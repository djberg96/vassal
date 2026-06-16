package VASSAL.build.module.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

class AbstractMetaDataTest {
  @Test
  void metadataParserCanBeUsedConcurrently() throws Exception {
    final List<Callable<String>> tasks = IntStream.range(0, 8)
      .mapToObj(i -> (Callable<String>) () -> parseName("module-" + i))
      .toList();

    try (var executor = Executors.newFixedThreadPool(4)) {
      final var results = executor.invokeAll(tasks);

      for (int i = 0; i < results.size(); ++i) {
        assertEquals("module-" + i, results.get(i).get());
      }
    }
  }

  private static String parseName(String name) throws Exception {
    final var handler = new NameHandler();
    final String xml = "<data><name>" + name + "</name></data>";

    AbstractMetaData.parse(handler, new InputSource(new StringReader(xml)));
    return handler.name;
  }

  private static final class NameHandler extends DefaultHandler {
    private final StringBuilder text = new StringBuilder();
    private String name;

    @Override
    public void characters(char[] ch, int start, int length) {
      text.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
      if ("name".equals(qName)) {
        name = text.toString();
      }
    }
  }
}
