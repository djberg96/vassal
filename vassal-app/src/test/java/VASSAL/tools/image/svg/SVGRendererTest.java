package VASSAL.tools.image.svg;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

  private static final String SVG_WITH_CLIPPED_USE = """
    <svg xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 50 50" width="50" height="50">
      <defs>
        <g id="flag">
          <linearGradient id="french_flag" x2="100%" y2="0%">
            <stop offset="33.3%" stop-color="blue"/>
            <stop offset="33.3%" stop-color="white"/>
            <stop offset="66.7%" stop-color="white"/>
            <stop offset="66.7%" stop-color="red"/>
          </linearGradient>
          <rect fill="url(#french_flag)" stroke="black" stroke-width="1" width="36" height="24"/>
          <rect id="border" width="36" height="24" fill="none" stroke="black" stroke-width="0.5"/>
          <clipPath id="clip">
            <use xlink:href="#border"/>
          </clipPath>
        </g>
      </defs>

      <rect width="50" height="50" fill="white" stroke="black" stroke-width="2"/>
      <use xlink:href="#flag" x="7" y="12" clip-path="url(#clip)"/>
    </svg>
    """;

  private static final String SVG_WITH_COMPATIBILITY_AOI = """
    <svg xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100" width="100" height="100">
      <defs>
        <rect id="clipRect" width="100" height="100"/>
        <clipPath id="clip">
          <use xlink:href="#clipRect"/>
        </clipPath>
      </defs>

      <rect width="100" height="100" fill="#00ff00" clip-path="url(#clip)"/>
      <rect x="40" y="40" width="20" height="20" fill="#ff0000"/>
    </svg>
    """;

  private static final String SVG_WITH_FILTERED_AOI = """
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100" width="100" height="100">
      <defs>
        <filter id="paper">
          <feTurbulence type="fractalNoise" baseFrequency="0.04"/>
        </filter>
      </defs>

      <rect width="100" height="100" fill="#00ff00" filter="url(#paper)"/>
      <rect x="40" y="40" width="20" height="20" fill="#ff0000"/>
    </svg>
    """;

  private static final String SVG_WITH_MARKER_EDGE_FLAG = """
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 60 60" width="60" height="60">
      <rect x="1" y="1" width="58" height="58" fill="#ffffff" stroke="#000000" stroke-width="2"/>
      <text x="30" y="13" text-anchor="middle" font-family="Serif" font-size="12">ALGARVE</text>
      <rect x="13" y="20" width="10" height="18" fill="#0000ff"/>
      <rect x="24" y="20" width="10" height="18" fill="#ffffff"/>
      <rect x="35" y="20" width="10" height="18" fill="#ff0000"/>
      <text x="30" y="52" text-anchor="middle" font-family="Serif" font-size="12">1</text>
    </svg>
    """;

  @Test
  void renderReturnsPixelsFromSvgImage() throws IOException {
    final BufferedImage image = renderer().render();

    assertNotNull(image);
    assertEquals(10, image.getWidth());
    assertEquals(10, image.getHeight());
    assertPixel(new Color(0, 255, 0, 255), image, 0, 0);
    assertPixel(new Color(255, 0, 0, 255), image, 3, 4);
  }

  @Test
  void renderAreaOfInterestReturnsPixelsFromSvgImage() throws IOException {
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

  @Test
  void simpleSvgDoesNotRequireCompatibilityRenderer() {
    assertFalse(SVGRenderer.needsCompatibilityRenderer(SVG));
  }

  @Test
  void filterHeavySvgUsesCompatibilityRenderer() {
    assertTrue(SVGRenderer.needsCompatibilityRenderer(SVG_WITH_FILTERED_AOI));
    assertTrue(SVGRenderer.containsFilterCompatibilityFeature(SVG_WITH_FILTERED_AOI));
  }

  @Test
  void clippedUseSvgRequiresCompatibilityRenderer() {
    assertTrue(SVGRenderer.needsCompatibilityRenderer(SVG_WITH_CLIPPED_USE));
    assertFalse(SVGRenderer.containsFilterCompatibilityFeature(SVG_WITH_CLIPPED_USE));
  }

  @Test
  void renderSupportsClippedUseWithoutCroppingRightEdge() throws IOException {
    final BufferedImage image = renderer(SVG_WITH_CLIPPED_USE).render();

    assertNotNull(image);
    assertEquals(50, image.getWidth());
    assertEquals(50, image.getHeight());
    assertPixel(new Color(0, 0, 255, 255), image, 12, 20);
    assertPixel(new Color(255, 0, 0, 255), image, 38, 20);
  }

  @Test
  void renderSupportsScaledClippedUseWithoutDoubleScalingContent() throws IOException {
    final BufferedImage image = renderer(SVG_WITH_CLIPPED_USE).render(0.0, 2.0);

    assertNotNull(image);
    assertEquals(100, image.getWidth());
    assertEquals(100, image.getHeight());
    assertPixel(new Color(0, 0, 255, 255), image, 24, 40);
    assertPixel(new Color(255, 0, 0, 255), image, 76, 40);
  }

  @Test
  void renderCompatibilityAreaOfInterestReturnsCroppedTile() throws IOException {
    final BufferedImage image = renderer(SVG_WITH_COMPATIBILITY_AOI).render(
      0.0, 1.0, new Rectangle2D.Float(40, 40, 20, 20)
    );

    assertNotNull(image);
    assertEquals(20, image.getWidth());
    assertEquals(20, image.getHeight());
    assertPixel(new Color(255, 0, 0, 255), image, 0, 0);
    assertPixel(new Color(255, 0, 0, 255), image, 19, 19);
  }

  @Test
  void renderCompatibilityScaledAreaOfInterestReturnsCroppedTile() throws IOException {
    final BufferedImage image = renderer(SVG_WITH_COMPATIBILITY_AOI).render(
      0.0, 2.0, new Rectangle2D.Float(80, 80, 40, 40)
    );

    assertNotNull(image);
    assertEquals(40, image.getWidth());
    assertEquals(40, image.getHeight());
    assertPixel(new Color(255, 0, 0, 255), image, 0, 0);
    assertPixel(new Color(255, 0, 0, 255), image, 39, 39);
  }

  @Test
  void renderFilterCompatibilityAreaOfInterestReturnsCroppedTile() throws IOException {
    final BufferedImage image = renderer(SVG_WITH_FILTERED_AOI).render(
      0.0, 1.0, new Rectangle2D.Float(40, 40, 20, 20)
    );

    assertNotNull(image);
    assertEquals(20, image.getWidth());
    assertEquals(20, image.getHeight());
    assertPixel(new Color(255, 0, 0, 255), image, 0, 0);
    assertPixel(new Color(255, 0, 0, 255), image, 19, 19);
  }

  @Test
  void renderMarkerStyleSvgDoesNotCropRightEdge() throws IOException {
    final BufferedImage image = renderer(SVG_WITH_MARKER_EDGE_FLAG).render();

    assertNotNull(image);
    assertEquals(60, image.getWidth());
    assertEquals(60, image.getHeight());
    assertPixel(new Color(0, 0, 255, 255), image, 18, 28);
    assertPixel(new Color(255, 0, 0, 255), image, 40, 28);
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
