package VASSAL.tools.image.tilecache;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TilesToImageTest {
  @TempDir
  private Path tempDir;

  @Test
  public void rebuildsImageFromTiles() throws IOException {
    final String base = tempDir.resolve("image").toString();
    writeTile(base, 0, 0, 2, 2, Color.RED);
    writeTile(base, 1, 0, 2, 2, Color.GREEN);
    writeTile(base, 2, 0, 1, 2, Color.BLUE);
    writeTile(base, 0, 1, 2, 1, Color.CYAN);
    writeTile(base, 1, 1, 2, 1, Color.MAGENTA);
    writeTile(base, 2, 1, 1, 1, Color.YELLOW);

    final Path output = tempDir.resolve("rebuilt.png");

    TilesToImage.main(new String[] { base, "1", output.toString() });

    final BufferedImage rebuilt = ImageIO.read(output.toFile());
    assertEquals(5, rebuilt.getWidth());
    assertEquals(3, rebuilt.getHeight());
    assertEquals(Color.RED.getRGB(), rebuilt.getRGB(0, 0));
    assertEquals(Color.GREEN.getRGB(), rebuilt.getRGB(2, 0));
    assertEquals(Color.BLUE.getRGB(), rebuilt.getRGB(4, 0));
    assertEquals(Color.CYAN.getRGB(), rebuilt.getRGB(0, 2));
    assertEquals(Color.MAGENTA.getRGB(), rebuilt.getRGB(2, 2));
    assertEquals(Color.YELLOW.getRGB(), rebuilt.getRGB(4, 2));
  }

  private static void writeTile(
    String base,
    int tileX,
    int tileY,
    int width,
    int height,
    Color color
  ) throws IOException {
    final BufferedImage image =
      new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

    for (int y = 0; y < height; ++y) {
      for (int x = 0; x < width; ++x) {
        image.setRGB(x, y, color.getRGB());
      }
    }

    TileUtils.write(
      image,
      new File(base + "(" + tileX + "," + tileY + ")@1:1")
    );
  }
}
