package VASSAL.preferences;

import VASSAL.build.MockModuleTest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BasicPreferenceTest extends MockModuleTest {

  @Test
  public void propertyIsCreatedWhenPreferenceNameIsSet() {
    final StringPreference preference = new StringPreference();

    assertNull(preference.property);

    preference.setAttribute(BasicPreference.NAME, "PreferenceName");

    assertEquals("PreferenceName", preference.getVariableName());
    assertNotNull(preference.property);
  }
}
