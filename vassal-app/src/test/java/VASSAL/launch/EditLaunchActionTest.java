package VASSAL.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import VASSAL.i18n.Resources;
import VASSAL.tools.imports.ImportAction;

import org.junit.jupiter.api.Test;

import javax.swing.Action;

import java.io.File;

public class EditLaunchActionTest {
  @Test
  public void editAndImportActionsUseTheirOwnNames() {
    assertEquals(
      Resources.getString("Main.edit_module"),
      new EditModuleAction((java.awt.Component) null).getValue(Action.NAME)
    );
    assertEquals(
      Resources.getString("Editor.edit_extension"),
      new EditExtensionAction((java.awt.Component) null).getValue(Action.NAME)
    );
    assertEquals(
      Resources.getString("Editor.import_module"),
      new ImportAction(null).getValue(Action.NAME)
    );
  }

  @Test
  public void editorLaunchActionsUseTheirOwnNames() {
    assertEquals(
      Resources.getString("Main.edit_module_specific"),
      new Editor.LaunchAction(null, new File("module.vmod")).getValue(Action.NAME)
    );
    assertEquals(
      Resources.getString("Main.edit_module_specific"),
      new Editor.ListLaunchAction(null, new File("module.vmod")).getValue(Action.NAME)
    );
    assertEquals(
      Resources.getString("Main.edit_module"),
      new Editor.PromptLaunchAction(null).getValue(Action.NAME)
    );
  }

  @Test
  public void editorLaunchActionIsDisabledWhenModuleIsInUse() {
    final File module = new File("module.vmod");
    AbstractLaunchAction.getUseTracker().incrementUsed(module);

    try {
      assertFalse(new Editor.LaunchAction(null, module).isEnabled());
      assertFalse(new Editor.ListLaunchAction(null, module).isEnabled());
    }
    finally {
      AbstractLaunchAction.getUseTracker().decrementUsed(module);
    }
  }
}
