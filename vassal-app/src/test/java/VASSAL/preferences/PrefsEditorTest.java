package VASSAL.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.swing.Action;

import org.junit.jupiter.api.Test;

class PrefsEditorTest {
  @Test
  void editActionUsesExplicitLocalizedMnemonic() {
    final Action action = new PrefsEditor().getEditAction();

    assertEquals((int) 'P', action.getValue(Action.MNEMONIC_KEY));
  }
}
