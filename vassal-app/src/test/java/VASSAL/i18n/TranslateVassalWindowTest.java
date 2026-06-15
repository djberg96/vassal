package VASSAL.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Locale;

import org.junit.jupiter.api.Test;

class TranslateVassalWindowTest {
  @Test
  void parsesLanguageOnlyPropertiesFileName() {
    assertEquals(
      Locale.ENGLISH,
      TranslateVassalWindow.localeFromPropertiesFileName("VASSAL_en.properties")
    );
  }

  @Test
  void parsesLanguageAndCountryPropertiesFileName() {
    assertEquals(
      Locale.CANADA_FRENCH,
      TranslateVassalWindow.localeFromPropertiesFileName("VASSAL_fr_CA.properties")
    );
  }

  @Test
  void normalizesLocaleCaseInPropertiesFileName() {
    assertEquals(
      Locale.of("de", "DE"),
      TranslateVassalWindow.localeFromPropertiesFileName("VASSAL_DE_de.properties")
    );
  }

  @Test
  void rejectsInvalidPropertiesFileName() {
    assertNull(TranslateVassalWindow.localeFromPropertiesFileName("module.properties"));
    assertNull(TranslateVassalWindow.localeFromPropertiesFileName("VASSAL_english.properties"));
    assertNull(TranslateVassalWindow.localeFromPropertiesFileName("VASSAL_en_US_extra.properties"));
  }
}
