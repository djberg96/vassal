package VASSAL.tools.image.svg;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SVGRendererTest {
  private static final String SVG = """
    <svg xmlns="http://www.w3.org/2000/svg" width="10" height="10">
      <rect x="0" y="0" width="10" height="10" fill="#00ff00"/>
      <rect x="2" y="3" width="4" height="3" fill="#ff0000"/>
    </svg>
    """;

  private static final String SVG_WITH_MODERN_HREF = """
    <svg xmlns="http://www.w3.org/2000/svg" width="10" height="10">
      <defs>
        <rect id="mark" x="0" y="0" width="10" height="10" fill="#0000ff"/>
      </defs>
      <use href="#mark"/>
    </svg>
    """;

  @Test
  void renderReturnsPixelsFromBatikOffscreenImage() throws IOException {
    final BufferedImage image = renderer().render();

    assertNotNull(image);
    assertEquals(10, image.getWidth());
    assertEquals(10, image.getHeight());
    assertPixel(new Color(0, 255, 0, 255), image, 0, 0);
    assertPixel(new Color(255, 0, 0, 255), image, 3, 4);
  }

  @Test
  void renderAreaOfInterestReturnsPixelsFromBatikOffscreenImage() throws IOException {
    final BufferedImage image = renderer().render(0.0, 1.0, new Rectangle2D.Float(2, 3, 4, 3));

    assertNotNull(image);
    assertEquals(4, image.getWidth());
    assertEquals(3, image.getHeight());
    assertPixel(new Color(255, 0, 0, 255), image, 0, 0);
    assertPixel(new Color(255, 0, 0, 255), image, 3, 2);
  }

  @Test
  void renderSupportsUseHrefWithoutExplicitXLinkNamespace() throws IOException {
    final BufferedImage image = renderer(SVG_WITH_MODERN_HREF).render();

    assertNotNull(image);
    assertEquals(10, image.getWidth());
    assertEquals(10, image.getHeight());
    assertPixel(new Color(0, 0, 255, 255), image, 5, 5);
  }

  private static SVGRenderer renderer() throws IOException {
    return renderer(SVG);
  }

  private static SVGRenderer renderer(String svg) throws IOException {
    return new SVGRenderer(
      "test.svg",
      new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8))
    );
  }

  private static void assertPixel(Color expected, BufferedImage image, int x, int y) {
    assertEquals(expected.getRGB(), image.getRGB(x, y));
  }
}
