package VASSAL.build.module.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Set;

import javax.swing.JPanel;

import VASSAL.build.GameModule;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class HidePiecesButtonTest {
  @Test
  void hidePiecesButtonIsNotASwingPanel() {
    assertFalse(JPanel.class.isAssignableFrom(HidePiecesButton.class));
  }

  @Test
  void configuredIconsAreIncludedInImageSearch() {
    try (MockedStatic<GameModule> staticGameModule = Mockito.mockStatic(GameModule.class)) {
      staticGameModule.when(GameModule::getGameModule).thenReturn(Mockito.mock(GameModule.class));
      final HidePiecesButton button = new HidePiecesButton();
      button.setAttribute(HidePiecesButton.SHOWING_ICON, "showing.png");
      button.setAttribute(HidePiecesButton.HIDDEN_ICON, "hidden.png");

      assertEquals(Set.of("showing.png", "hidden.png"), button.getLocalImageNames());
    }
  }

  @Test
  void keepsLegacyTranslationPrefix() {
    try (MockedStatic<GameModule> staticGameModule = Mockito.mockStatic(GameModule.class)) {
      staticGameModule.when(GameModule::getGameModule).thenReturn(Mockito.mock(GameModule.class));
      final HidePiecesButton button = new HidePiecesButton();

      assertEquals("HidePieces", button.getI18nData().getPrefix());
    }
  }
}
