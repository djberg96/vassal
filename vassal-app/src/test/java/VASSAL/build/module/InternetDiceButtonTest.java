package VASSAL.build.module;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class InternetDiceButtonTest {
  private static class TestInternetDiceButton extends InternetDiceButton {
    private boolean rolledLocally;
    private boolean rolledWithInternetDice;

    @Override
    protected void initLaunchButton() {
    }

    void roll() {
      DR();
    }

    boolean rolledLocally() {
      return rolledLocally;
    }

    boolean rolledWithInternetDice() {
      return rolledWithInternetDice;
    }

    @Override
    protected void rollLocally() {
      rolledLocally = true;
    }

    @Override
    protected void rollWithInternetDice() {
      rolledWithInternetDice = true;
    }
  }

  @Test
  public void internetDiceServerSettingsAreNotModuleAttributes() {
    final TestInternetDiceButton button = new TestInternetDiceButton();

    assertFalse(Arrays.asList(button.getAttributeNames()).contains("diceServer"));
    assertFalse(Arrays.asList(button.getAttributeNames()).contains("serverApiKey"));
  }

  @Test
  public void legacyInternetDiceServerSettingsAreIgnored() {
    final TestInternetDiceButton button = new TestInternetDiceButton();

    button.setAttribute("diceServer", DieManager.Q_RANDOM_DESCRIPTION);
    button.setAttribute("serverApiKey", "secret");

    assertFalse(Arrays.asList(button.getAttributeNames()).contains("diceServer"));
    assertFalse(Arrays.asList(button.getAttributeNames()).contains("serverApiKey"));
  }

  @Test
  public void rollsWithInternetDiceWhenConfiguredServerIsUsable() {
    final DieManager oldDieManager = InternetDiceButton.dieManager;
    InternetDiceButton.dieManager = mock(DieManager.class);
    when(InternetDiceButton.dieManager.canUseInternetDice()).thenReturn(true);
    final TestInternetDiceButton button = new TestInternetDiceButton();

    try {
      button.roll();

      assertTrue(button.rolledWithInternetDice());
      assertFalse(button.rolledLocally());
    }
    finally {
      InternetDiceButton.dieManager = oldDieManager;
    }
  }

  @Test
  public void rollsLocallyWhenConfiguredServerIsNotUsable() {
    final DieManager oldDieManager = InternetDiceButton.dieManager;
    InternetDiceButton.dieManager = mock(DieManager.class);
    when(InternetDiceButton.dieManager.canUseInternetDice()).thenReturn(false);
    final TestInternetDiceButton button = new TestInternetDiceButton();

    try {
      button.roll();

      assertTrue(button.rolledLocally());
      assertFalse(button.rolledWithInternetDice());
    }
    finally {
      InternetDiceButton.dieManager = oldDieManager;
    }
  }
}
