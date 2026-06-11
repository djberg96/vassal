package VASSAL.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import VASSAL.i18n.Resources;
import VASSAL.tools.imports.ImportAction;

import org.junit.jupiter.api.Test;

import javax.swing.Action;

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
}
