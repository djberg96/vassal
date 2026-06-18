package VASSAL.build.module;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import VASSAL.build.GameModule;
import VASSAL.configure.Configurer;
import VASSAL.preferences.Prefs;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class DieManagerTest {
  @Test
  public void knownServerNamesAndDescriptionsAreReturnedAsArrays() {
    final GameModule gameModule = mock(GameModule.class);
    final Prefs prefs = mock(Prefs.class);
    when(gameModule.getPrefs()).thenReturn(prefs);

    try (MockedStatic<GameModule> staticGameModule = Mockito.mockStatic(GameModule.class)) {
      staticGameModule.when(GameModule::getGameModule).thenReturn(gameModule);

      final DieManager manager = new DieManager();

      assertArrayEquals(new String[] { "RandomOrg", "QRandom" }, manager.getNames());
      assertArrayEquals(
        new String[] { "random.org", "qrandom.io" },
        manager.getDescriptions()
      );
    }
  }

  @Test
  public void moduleEmailPreferencesAreNotRequiredForConstruction() {
    final GameModule gameModule = mock(GameModule.class);
    final Prefs prefs = mock(Prefs.class);
    when(gameModule.getPrefs()).thenReturn(prefs);

    try (MockedStatic<GameModule> staticGameModule = Mockito.mockStatic(GameModule.class)) {
      staticGameModule.when(GameModule::getGameModule).thenReturn(gameModule);

      assertDoesNotThrow(DieManager::new);
    }
  }

  @Test
  public void internetDiceServerSettingsAreRegisteredAsGlobalPreferences() {
    final Prefs prefs = mock(Prefs.class);
    final ArgumentCaptor<Configurer> configurer = ArgumentCaptor.forClass(Configurer.class);

    DieManager.addGlobalPreferences(prefs);

    verify(prefs, times(2)).addOption(Mockito.eq("Internet Dice"), configurer.capture());
    assertEquals(DieManager.DICE_SERVER, configurer.getAllValues().get(0).getKey());
    assertEquals(DieManager.SERVER_PW, configurer.getAllValues().get(1).getKey());
  }

  @Test
  public void internetDiceApiKeyIsTrimmed() {
    final DieManager.TrimmingPasswordConfigurer configurer =
      new DieManager.TrimmingPasswordConfigurer(mock(Prefs.class), DieManager.SERVER_PW, "API key / password", "");

    configurer.setValue("  secret  ");

    assertEquals("secret", configurer.getValueString());
  }

  @Test
  public void randomOrgVerificationRequiresApiKey() {
    final Prefs prefs = mock(Prefs.class);
    when(prefs.getValue(DieManager.DICE_SERVER)).thenReturn(DieManager.RANDOM_ORG_DESCRIPTION);
    when(prefs.getValue(DieManager.SERVER_PW)).thenReturn(" ");

    final Exception e = assertThrows(Exception.class, () -> DieManager.verifyInternetDice(prefs));

    assertEquals("An API key is required for the selected service.", e.getMessage());
  }
}
