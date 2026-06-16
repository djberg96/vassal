package VASSAL.tools.imageop;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AbstractOpImplTest {
  @AfterEach
  void clearInterruptedStatus() {
    Thread.interrupted();
  }

  @Test
  void getImageReturnsNullWhenEvaluationIsCancelled() {
    final ImageOp op = new ThrowingImageOp(new CancellationException("cancelled"));

    assertNull(op.getImage());
  }

  @Test
  void getImageRestoresInterruptedStatusWhenEvaluationIsInterrupted() {
    final ImageOp op = new ThrowingImageOp(new InterruptedException("interrupted"));

    assertNull(op.getImage());
    assertTrue(Thread.currentThread().isInterrupted());
  }

  private static final class ThrowingImageOp extends AbstractOpImpl {
    private final Exception exception;

    private ThrowingImageOp(Exception exception) {
      this.exception = exception;
    }

    @Override
    public List<VASSAL.tools.opcache.Op<?>> getSources() {
      return List.of();
    }

    @Override
    public BufferedImage eval() throws Exception {
      throw exception;
    }

    @Override
    protected void fixSize() {
      size = new Dimension(1, 1);
    }

    @Override
    public Dimension getTileSize() {
      return new Dimension(1, 1);
    }

    @Override
    public int getTileHeight() {
      return 1;
    }

    @Override
    public int getTileWidth() {
      return 1;
    }

    @Override
    public int getNumXTiles() {
      return 1;
    }

    @Override
    public int getNumYTiles() {
      return 1;
    }

    @Override
    public BufferedImage getTile(int tileX, int tileY, ImageOpObserver obs)
      throws CancellationException, InterruptedException, ExecutionException {

      return getImage(obs);
    }

    @Override
    public Future<BufferedImage> getFutureTile(int tileX, int tileY, ImageOpObserver obs)
      throws ExecutionException {

      return getFutureImage(obs);
    }

    @Override
    public ImageOp getTileOp(int tileX, int tileY) {
      return this;
    }

    @Override
    public Point[] getTileIndices(Rectangle rect) {
      return new Point[]{new Point(0, 0)};
    }
  }
}
