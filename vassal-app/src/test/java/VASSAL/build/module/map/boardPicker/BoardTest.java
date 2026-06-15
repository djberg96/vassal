package VASSAL.build.module.map.boardPicker;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardTest {
  private static class TestBoard extends Board {
    void draw(Future<BufferedImage> future) {
      drawTile(null, future, 0, 0, null);
    }
  }

  private abstract static class ThrowingFuture implements Future<BufferedImage> {
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
    }

    @Override
    public boolean isCancelled() {
      return false;
    }

    @Override
    public boolean isDone() {
      return true;
    }

    @Override
    public BufferedImage get(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
      return get();
    }
  }

  @Test
  void drawTileIgnoresCancelledFuture() {
    final TestBoard board = new TestBoard();

    assertDoesNotThrow(() -> board.draw(new ThrowingFuture() {
      @Override
      public BufferedImage get() {
        throw new CancellationException();
      }
    }));
  }

  @Test
  void drawTileRestoresInterruptStatusWhenInterrupted() {
    final TestBoard board = new TestBoard();

    try {
      board.draw(new ThrowingFuture() {
        @Override
        public BufferedImage get() throws InterruptedException {
          throw new InterruptedException();
        }
      });

      assertTrue(Thread.currentThread().isInterrupted());
    }
    finally {
      Thread.interrupted();
    }
  }
}
