package VASSAL.build.module.map.boardPicker.board.mapgrid;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZoneHighlightTest {
  private static BufferedImage render(ZoneHighlight highlight) {
    final BufferedImage image = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g = image.createGraphics();
    try {
      highlight.draw(g, new Rectangle(0, 0, image.getWidth(), image.getHeight()), 1.0);
    }
    finally {
      g.dispose();
    }
    return image;
  }

  private static boolean containsColor(BufferedImage image, Color color) {
    final int expected = color.getRGB() & 0x00ffffff;
    for (int y = 0; y < image.getHeight(); ++y) {
      for (int x = 0; x < image.getWidth(); ++x) {
        final int argb = image.getRGB(x, y);
        if (((argb >>> 24) > 0) && ((argb & 0x00ffffff) == expected)) {
          return true;
        }
      }
    }
    return false;
  }

  @Test
  void changingColorInvalidatesGeneratedPatternPaint() {
    final ZoneHighlight highlight = new ZoneHighlight();
    highlight.setAttribute(ZoneHighlight.STYLE, ZoneHighlight.STYLE_STRIPES);
    highlight.setAttribute(ZoneHighlight.COLOR, Color.RED);

    assertTrue(containsColor(render(highlight), Color.RED));

    highlight.setAttribute(ZoneHighlight.COLOR, Color.BLUE);
    final BufferedImage updated = render(highlight);

    assertTrue(containsColor(updated, Color.BLUE));
    assertFalse(containsColor(updated, Color.RED));
  }
}
