package VASSAL.tools.opcache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.List;
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
}
