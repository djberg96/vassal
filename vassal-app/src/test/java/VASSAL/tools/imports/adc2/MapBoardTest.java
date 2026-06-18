package VASSAL.tools.imports.adc2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.Font;
import java.awt.font.TextAttribute;

import org.junit.jupiter.api.Test;

class MapBoardTest {
  private static final int ARIAL = 9;
  private static final int UNDERLINE_FLAG = 0x40;

  @Test
  void getDefaultFontPreservesUnderlineFlag() {
    final Font font = MapBoard.getDefaultFont(12, ARIAL | UNDERLINE_FLAG);

    assertEquals(TextAttribute.UNDERLINE_ON, font.getAttributes().get(TextAttribute.UNDERLINE));
  }

  @Test
  void getDefaultFontDoesNotUnderlineByDefault() {
    final Font font = MapBoard.getDefaultFont(12, ARIAL);

    assertNotEquals(TextAttribute.UNDERLINE_ON, font.getAttributes().get(TextAttribute.UNDERLINE));
  }
}
