package VASSAL.tools.opcache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

public class OpCacheTest {
  @Test
  public void synchronousResultsAreCached() {
    final CountingOp op = new CountingOp(new OpCache());

    assertThat(op.get(), is(equalTo(1)));
    assertThat(op.get(), is(equalTo(1)));
    assertThat(op.evaluations(), is(equalTo(1)));
  }

  @Test
  public void getIfDoneReturnsCompletedSynchronousResult() {
    final OpCache cache = new OpCache();
    final CountingOp op = new CountingOp(cache);
    final OpCache.Key<Integer> key = op.newKey();

    assertThat(cache.getIfDone(key), is(equalTo(null)));

    assertThat(cache.get(key), is(equalTo(1)));
    assertThat(cache.getIfDone(key), is(equalTo(1)));
  }

  @Test
  public void synchronousFailuresAreCached() {
    final OpCache cache = new OpCache();
    final FailingOp op = new FailingOp(cache);
    final OpCache.Key<Integer> key = op.newKey();

    assertThrows(ExecutionException.class, () -> cache.get(key, null));
    assertThrows(ExecutionException.class, () -> cache.get(key, null));

    assertThat(op.evaluations(), is(equalTo(1)));
  }

  @Test
  public void clearCancelsQueuedRequests() throws Exception {
    final OpCache cache = new OpCache();
    final CountDownLatch release = new CountDownLatch(1);
    final BlockingOp first = new BlockingOp(cache, release);
    final BlockingOp second = new BlockingOp(cache, release);
    final CountingOp queued = new CountingOp(cache);
    final NoOpObserver<Integer> observer = new NoOpObserver<>();

    final Future<Integer> firstFuture = cache.getFuture(first.newKey(), observer);
    final Future<Integer> secondFuture = cache.getFuture(second.newKey(), observer);
    assertTrue(first.awaitStarted());
    assertTrue(second.awaitStarted());

    final Future<Integer> queuedFuture = cache.getFuture(queued.newKey(), observer);

    try {
      cache.clear();

      assertTrue(firstFuture.isCancelled());
      assertTrue(secondFuture.isCancelled());
      assertTrue(queuedFuture.isCancelled());
      assertThat(queued.evaluations(), is(equalTo(0)));
    }
    finally {
      release.countDown();
    }
  }

  private static final class CountingOp extends AbstractOpImpl<Integer> {
    private final AtomicInteger evaluations = new AtomicInteger();

    private CountingOp(OpCache cache) {
      super(cache);
    }

    @Override
    public List<Op<?>> getSources() {
      return List.of();
    }

    @Override
    public Integer eval() {
      return evaluations.incrementAndGet();
    }

    private int evaluations() {
      return evaluations.get();
    }
  }

  private static final class FailingOp extends AbstractOpImpl<Integer> {
    private final AtomicInteger evaluations = new AtomicInteger();

    private FailingOp(OpCache cache) {
      super(cache);
    }

    @Override
    public List<Op<?>> getSources() {
      return List.of();
    }

    @Override
    public Integer eval() throws Exception {
      evaluations.incrementAndGet();
      throw new Exception("failed");
    }

    private int evaluations() {
      return evaluations.get();
    }
  }

  private static final class BlockingOp extends AbstractOpImpl<Integer> {
    private final CountDownLatch started = new CountDownLatch(1);
    private final CountDownLatch release;

    private BlockingOp(OpCache cache, CountDownLatch release) {
      super(cache);
      this.release = release;
    }

    @Override
    public List<Op<?>> getSources() {
      return List.of();
    }

    @Override
    public Integer eval() throws InterruptedException {
      started.countDown();
      release.await();
      return 1;
    }

    private boolean awaitStarted() throws InterruptedException {
      return started.await(5, TimeUnit.SECONDS);
    }
  }

  private static final class NoOpObserver<V> implements OpObserver<V> {
    @Override
    public void succeeded(Op<V> op, V val) {
    }

    @Override
    public void cancelled(Op<V> op, java.util.concurrent.CancellationException e) {
    }

    @Override
    public void interrupted(Op<V> op, InterruptedException e) {
    }

    @Override
    public void failed(Op<V> op, ExecutionException e) {
    }
  }
}
