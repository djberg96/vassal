package VASSAL.configure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JTextField;

import org.junit.jupiter.api.Test;

class SoundConfigurerTest {
  @Test
  void disabledSoundUsesLocalizedDisplayTextButStableSerializedValue() {
    final SoundConfigurer configurer = new SoundConfigurer("key", "name", "phone1.wav");

    configurer.setValue("<disabled>");
    configurer.getControls();

    final JTextField field = findComponent(configurer.getControls(), JTextField.class);
    assertNotNull(field);
    assertEquals("Disabled", field.getText());
    assertEquals("<disabled>", configurer.getValueString());
  }

  private static <T extends Component> T findComponent(Component component, Class<T> type) {
    if (type.isInstance(component)) {
      return type.cast(component);
    }

    if (component instanceof Container container) {
      for (Component child : container.getComponents()) {
        final T match = findComponent(child, type);
        if (match != null) {
          return match;
        }
      }
    }

    return null;
  }
}
