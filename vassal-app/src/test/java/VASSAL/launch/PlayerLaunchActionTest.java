package VASSAL.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import VASSAL.i18n.Resources;

import org.junit.jupiter.api.Test;

import javax.swing.Action;

import java.io.File;

public class PlayerLaunchActionTest {
  @Test
  public void promptLaunchActionUsesPromptName() {
    final Action action = new Player.PromptLaunchAction(null);

    assertEquals(Resources.getString("Main.play_module"), action.getValue(Action.NAME));
  }

  @Test
  public void launchActionIsDisabledWhenModuleIsBeingEdited() {
    final File module = new File("module.vmod");
    AbstractLaunchAction.getUseTracker().markEditing(module);

    try {
      final Action action = new Player.LaunchAction(null, module);

      assertFalse(action.isEnabled());
    }
    finally {
      AbstractLaunchAction.getUseTracker().unmarkEditing(module);
    }
  }
}
