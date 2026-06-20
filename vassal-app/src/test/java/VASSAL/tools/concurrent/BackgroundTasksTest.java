package VASSAL.tools.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

public class BackgroundTasksTest {
  @Test
  public void runsTaskOnVirtualThreadAndCallbackOnEdt() throws Exception {
    final CountDownLatch done = new CountDownLatch(1);
    final AtomicBoolean ranOnVirtualThread = new AtomicBoolean(false);
    final AtomicBoolean callbackOnEdt = new AtomicBoolean(false);
    final AtomicReference<String> result = new AtomicReference<>();
    final AtomicReference<Throwable> error = new AtomicReference<>();

    BackgroundTasks.submit(
      () -> {
        ranOnVirtualThread.set(Thread.currentThread().isVirtual());
        return "finished"; //NON-NLS
      },
      value -> {
        callbackOnEdt.set(SwingUtilities.isEventDispatchThread());
        result.set(value);
        done.countDown();
      },
      failure -> {
        error.set(failure);
        done.countDown();
      }
    );

    assertTrue(done.await(5, TimeUnit.SECONDS));
    assertNull(error.get());
    assertTrue(ranOnVirtualThread.get());
    assertTrue(callbackOnEdt.get());
    assertEquals("finished", result.get()); //NON-NLS
  }
}
