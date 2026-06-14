package VASSAL.tools.concurrent.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

public class DefaultMultiEventListenerSupportTest {
  private static class BaseEvent {
  }

  private static class SubEvent extends BaseEvent {
  }

  @Test
  public void getsListenersRegisteredForEventSupertypes() {
    final Object source = new Object();
    final MultiEventListenerSupport support =
      new DefaultMultiEventListenerSupport(source);
    final EventListener<BaseEvent> listener = (src, event) -> {
    };

    support.addEventListener(BaseEvent.class, listener);

    final List<EventListener<? super SubEvent>> listeners =
      support.getEventListeners(SubEvent.class);

    assertEquals(1, listeners.size());
    assertSame(listener, listeners.get(0));
  }

  @Test
  public void notifiesListenersRegisteredForEventSupertypes() {
    final Object source = new Object();
    final MultiEventListenerSupport support =
      new DefaultMultiEventListenerSupport(source);
    final SubEvent event = new SubEvent();
    final AtomicInteger calls = new AtomicInteger();

    support.addEventListener(BaseEvent.class, (src, received) -> {
      assertSame(source, src);
      assertSame(event, received);
      calls.incrementAndGet();
    });

    assertTrue(support.hasEventListeners(SubEvent.class));
    support.notify(event);

    assertTrue(support.hasEventListeners(SubEvent.class));
    assertEquals(1, calls.get());
  }
}
