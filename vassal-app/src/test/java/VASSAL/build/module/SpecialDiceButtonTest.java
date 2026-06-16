package VASSAL.build.module;

import static org.junit.jupiter.api.Assertions.assertFalse;

import javax.swing.ImageIcon;

import org.junit.jupiter.api.Test;

class SpecialDiceButtonTest {
  @Test
  void graphicalResultsIconDoesNotAdvertiseANullBackedImageIcon() throws Exception {
    final Class<?> resultsIconClass =
      Class.forName("VASSAL.build.module.SpecialDiceButton$ResultsIcon");

    assertFalse(ImageIcon.class.isAssignableFrom(resultsIconClass));
  }
}
