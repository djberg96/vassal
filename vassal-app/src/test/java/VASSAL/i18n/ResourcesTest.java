package VASSAL.i18n;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.junit.jupiter.api.Test;


public class ResourcesTest {
  @Test
  public void canSetLocale() {
    Locale locale = Resources.getLocale();
    assertNotNull(locale);
  }

  @Test
  public void formatsSoundErrorMessages() {
    assertEquals(
      "Error establishing audio stream for sounds/test.mp3",
      Resources.getString("Error.player_setup_failed", "sounds/test.mp3")
    );
  }
}
