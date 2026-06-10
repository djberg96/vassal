package VASSAL.configure;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ConfigurerLifecycleTest {

  @Test
  public void constructorStoresInitialValueWithoutCallingOverride() {
    final ConstructorTrackingConfigurer config =
      new ConstructorTrackingConfigurer("key", "name", "initial");

    assertEquals("initial", config.getValue());
    assertFalse(config.setValueCalled);
  }

  @Test
  public void propertyChangeSupportUsesConfigurerAsEventSource() {
    final ConstructorTrackingConfigurer config =
      new ConstructorTrackingConfigurer("key", "name", "initial");
    final AtomicReference<PropertyChangeEvent> event = new AtomicReference<>();

    config.addPropertyChangeListener(event::set);
    config.setValue("updated");

    assertSame(config, event.get().getSource());
    assertEquals("updated", event.get().getNewValue());
  }

  private static class ConstructorTrackingConfigurer extends Configurer {
    private boolean setValueCalled;

    ConstructorTrackingConfigurer(String key, String name, Object val) {
      super(key, name, val);
    }

    @Override
    public String getValueString() {
      return (String) getValue();
    }

    @Override
    public void setValue(Object o) {
      setValueCalled = true;
      super.setValue(o);
    }

    @Override
    public void setValue(String s) {
      setValue((Object) s);
    }

    @Override
    public Component getControls() {
      return new JPanel();
    }
  }
}
