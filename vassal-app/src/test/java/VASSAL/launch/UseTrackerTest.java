package VASSAL.launch;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UseTrackerTest {

  @Test
  public void tracksUsedFilesAndUsesTrackerAsEventSource() {
    final UseTracker tracker = new UseTracker();
    final File file = new File("module.vmod");
    final AtomicReference<PropertyChangeEvent> event = new AtomicReference<>();

    tracker.addPropertyChangeListener(event::set);
    tracker.incrementUsed(file);

    assertTrue(tracker.isInUse(file));
    assertTrue(tracker.anyInUse());
    assertSame(tracker, event.get().getSource());

    tracker.decrementUsed(file);

    assertFalse(tracker.isInUse(file));
    assertFalse(tracker.anyInUse());
    assertSame(tracker, event.get().getSource());
  }
}
