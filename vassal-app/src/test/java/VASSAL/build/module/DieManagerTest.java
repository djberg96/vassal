package VASSAL.build.module;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import VASSAL.build.GameModule;
import VASSAL.preferences.Prefs;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class DieManagerTest {
  @Test
  public void knownServerNamesAndDescriptionsAreReturnedAsArrays() {
    final GameModule gameModule = mock(GameModule.class);
    final Prefs prefs = mock(Prefs.class);
    when(gameModule.getPrefs()).thenReturn(prefs);
    when(prefs.getValue(DieManager.ADDRESS_BOOK)).thenReturn(new String[0]);
    when(prefs.getValue(DieManager.SECONDARY_EMAIL)).thenReturn("");

    try (MockedStatic<GameModule> staticGameModule = Mockito.mockStatic(GameModule.class)) {
      staticGameModule.when(GameModule::getGameModule).thenReturn(gameModule);

      final DieManager manager = new DieManager();

      assertArrayEquals(new String[] { "Bones" }, manager.getNames());
      assertArrayEquals(new String[] { "Bones Dice Server" }, manager.getDescriptions());
    }
  }
}
