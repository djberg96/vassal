package VASSAL.tools.imageop;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

  @Test
  void sizeProbeFailureDoesNotCacheEmptySize() {
    final RetryableSizeImageOp op = new RetryableSizeImageOp();

    assertEquals(new Dimension(), op.getSize());
    assertEquals(new Dimension(12, 34), op.getSize());
    assertEquals(12, op.getWidth());
    assertEquals(34, op.getHeight());
    assertEquals(1, op.getNumXTiles());
  }

  @Test
  void unknownSizeReturnsSafeEmptyValues() {
    final UnknownSizeImageOp op = new UnknownSizeImageOp();

    assertEquals(new Dimension(), op.getSize());
    assertEquals(0, op.getWidth());
    assertEquals(0, op.getHeight());
    assertEquals(0, op.getNumXTiles());
    assertEquals(0, op.getNumYTiles());
    assertEquals(0, op.getTileIndices(new Rectangle(0, 0, 20, 20)).length);
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

  private static final class RetryableSizeImageOp extends AbstractTiledOpImpl {
    private int sizeAttempts;

    @Override
    public List<VASSAL.tools.opcache.Op<?>> getSources() {
      return List.of();
    }

    @Override
    public BufferedImage eval() {
      return new BufferedImage(12, 34, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    protected void fixSize() {
      if (++sizeAttempts > 1) {
        size = new Dimension(12, 34);
      }
    }

    @Override
    protected ImageOp createTileOp(int tileX, int tileY) {
      return this;
    }
  }

  private static final class UnknownSizeImageOp extends AbstractTiledOpImpl {
    @Override
    public List<VASSAL.tools.opcache.Op<?>> getSources() {
      return List.of();
    }

    @Override
    public BufferedImage eval() {
      return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    protected void fixSize() {
    }

    @Override
    protected ImageOp createTileOp(int tileX, int tileY) {
      return this;
    }
  }
}
