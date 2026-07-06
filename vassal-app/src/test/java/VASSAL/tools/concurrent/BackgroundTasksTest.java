package VASSAL.tools.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
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

    BackgroundTasks.submitWithCallbacksOnEdt(
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

  @Test
  public void namedTaskContextIsScopedToBackgroundTask() throws Exception {
    final CountDownLatch done = new CountDownLatch(1);
    final AtomicReference<String> taskName = new AtomicReference<>();
    final AtomicReference<String> callbackName = new AtomicReference<>();
    final AtomicReference<Throwable> error = new AtomicReference<>();

    BackgroundTasks.submitWithCallbacksOnEdt(
      "test-background-task", //NON-NLS
      () -> {
        taskName.set(BackgroundTasks.currentContext().name());
        return null;
      },
      ignored -> {
        callbackName.set(BackgroundTasks.currentContext().name());
        done.countDown();
      },
      failure -> {
        error.set(failure);
        done.countDown();
      }
    );

    assertTrue(done.await(5, TimeUnit.SECONDS));
    assertNull(error.get());
    assertEquals("test-background-task", taskName.get()); //NON-NLS
    assertEquals("unnamed", callbackName.get()); //NON-NLS
  }

  @Test
  public void successCallbackRunsAfterTaskCompletes() throws Exception {
    final CountDownLatch done = new CountDownLatch(1);
    final List<String> events = new CopyOnWriteArrayList<>();

    BackgroundTasks.submitWithCallbacksOnEdt(
      () -> {
        events.add("task-start"); //NON-NLS
        events.add("task-end"); //NON-NLS
        return "result"; //NON-NLS
      },
      value -> {
        events.add("success-" + value); //NON-NLS
        done.countDown();
      },
      failure -> {
        events.add("failure"); //NON-NLS
        done.countDown();
      }
    );

    assertTrue(done.await(5, TimeUnit.SECONDS));
    assertEquals(List.of("task-start", "task-end", "success-result"), events);
  }

  @Test
  public void failureCallbackReceivesThrownExceptionOnEdt() throws Exception {
    final CountDownLatch done = new CountDownLatch(1);
    final IllegalStateException expected = new IllegalStateException("boom"); //NON-NLS
    final AtomicBoolean callbackOnEdt = new AtomicBoolean(false);
    final AtomicBoolean successCalled = new AtomicBoolean(false);
    final AtomicReference<Throwable> error = new AtomicReference<>();

    BackgroundTasks.submitWithCallbacksOnEdt(
      () -> {
        throw expected;
      },
      ignored -> {
        successCalled.set(true);
        done.countDown();
      },
      failure -> {
        callbackOnEdt.set(SwingUtilities.isEventDispatchThread());
        error.set(failure);
        done.countDown();
      }
    );

    assertTrue(done.await(5, TimeUnit.SECONDS));
    assertFalse(successCalled.get());
    assertTrue(callbackOnEdt.get());
    assertSame(expected, error.get());
  }

  @Test
  public void cancellationInterruptsTaskAndReportsFailureOnEdt() throws Exception {
    final CountDownLatch started = new CountDownLatch(1);
    final CountDownLatch done = new CountDownLatch(1);
    final AtomicBoolean callbackOnEdt = new AtomicBoolean(false);
    final AtomicReference<Throwable> error = new AtomicReference<>();

    final Future<?> future = BackgroundTasks.submitWithCallbacksOnEdt(
      () -> {
        started.countDown();
        Thread.sleep(TimeUnit.SECONDS.toMillis(30));
        return "should not finish"; //NON-NLS
      },
      ignored -> done.countDown(),
      failure -> {
        callbackOnEdt.set(SwingUtilities.isEventDispatchThread());
        error.set(failure);
        done.countDown();
      }
    );

    assertTrue(started.await(5, TimeUnit.SECONDS));
    assertTrue(future.cancel(true));
    assertTrue(done.await(5, TimeUnit.SECONDS));
    assertTrue(callbackOnEdt.get());
    assertTrue(error.get() instanceof InterruptedException);
    assertTrue(future.isCancelled());
  }
}
