package VASSAL.build.module.properties;

import java.beans.PropertyChangeEvent;
import java.util.concurrent.atomic.AtomicReference;

import VASSAL.build.MockModuleTest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class MutablePropertyTest extends MockModuleTest {

  @Test
  public void implementationUsesProvidedEventSource() {
    final Object source = new Object();
    final MutableProperty.Impl property = new MutableProperty.Impl("property", source);
    final AtomicReference<PropertyChangeEvent> event = new AtomicReference<>();

    property.addMutablePropertyChangeListener(event::set);
    property.setPropertyValue("value");

    assertSame(source, event.get().getSource());
  }
}
