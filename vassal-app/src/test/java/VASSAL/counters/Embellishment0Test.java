package VASSAL.counters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Embellishment0Test {
  @Test
  void editorPreservesOffsetValues() {
    final Embellishment0 layer = new Embellishment0();
    layer.xOff = 17;
    layer.yOff = -9;

    final String type = layer.getEditor().getType();
    final Embellishment0 copy = new Embellishment0(type, null);

    assertEquals(17, copy.xOff);
    assertEquals(-9, copy.yOff);
  }
}
