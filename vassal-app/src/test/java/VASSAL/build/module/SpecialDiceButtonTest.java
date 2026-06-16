package VASSAL.build.module;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.ImageIcon;

import org.junit.jupiter.api.Test;

class SpecialDiceButtonTest {
  @Test
  void graphicalResultsIconCanBeDisabledBySwing() throws Exception {
    final Class<?> resultsIconClass =
      Class.forName("VASSAL.build.module.SpecialDiceButton$ResultsIcon");

    assertTrue(ImageIcon.class.isAssignableFrom(resultsIconClass));
  }
}
