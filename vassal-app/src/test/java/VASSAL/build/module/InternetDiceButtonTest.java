package VASSAL.build.module;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class InternetDiceButtonTest {
  private static class TestInternetDiceButton extends InternetDiceButton {
    @Override
    protected void initLaunchButton() {
    }
  }

  @Test
  public void internetDiceServerSettingsAreConfigurableAttributes() {
    final InternetDiceButton button = new TestInternetDiceButton();

    assertTrue(Arrays.asList(button.getAttributeNames()).contains(InternetDiceButton.DICE_SERVER));
    assertTrue(Arrays.asList(button.getAttributeNames()).contains(InternetDiceButton.SERVER_API_KEY));
    assertEquals(DieManager.DEFAULT_DICE_SERVER, button.getAttributeValueString(InternetDiceButton.DICE_SERVER));
  }

  @Test
  public void internetDiceServerChoiceUsesKnownProviders() {
    final InternetDiceButton.InternetDiceServerConfig factory = new InternetDiceButton.InternetDiceServerConfig();

    assertArrayEquals(
      DieManager.getAvailableServerDescriptions(),
      ((VASSAL.configure.StringEnumConfigurer) factory.getConfigurer(null, "server", "Server")).getValidValues()
    );
  }

  @Test
  public void internetDiceServerSettingsRoundTripAsAttributes() {
    final InternetDiceButton button = new TestInternetDiceButton();

    button.setAttribute(InternetDiceButton.DICE_SERVER, DieManager.Q_RANDOM_DESCRIPTION);
    button.setAttribute(InternetDiceButton.SERVER_API_KEY, "secret");

    assertEquals(DieManager.Q_RANDOM_DESCRIPTION, button.getAttributeValueString(InternetDiceButton.DICE_SERVER));
    assertEquals("secret", button.getAttributeValueString(InternetDiceButton.SERVER_API_KEY));
  }
}
