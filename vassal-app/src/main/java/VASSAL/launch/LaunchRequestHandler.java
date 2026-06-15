/*
 * Copyright (c) 2000-2008 by Rodney Kinney, Joel Uckelman
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package VASSAL.launch;

import VASSAL.build.module.metadata.AbstractMetaData;
import VASSAL.build.module.metadata.MetaDataFactory;
import VASSAL.build.module.metadata.SaveMetaData;

public final class LaunchRequestHandler implements Runnable {
  private static final String MODULE_OPEN_FOR_EDITING =
    "The module is already open for editing."; //NON-NLS
  private static final String MODULE_OPEN_FOR_PLAY =
    "The module is already open for play."; //NON-NLS
  private static final String MODULE_NOT_FOUND =
    "Cannot find the module for this request."; //NON-NLS
  private static final String EXTENSION_OPEN =
    "The module or extension is already open."; //NON-NLS
  private static final String UNRECOGNIZED_MODE =
    "Unrecognized launch mode."; //NON-NLS

  private final LaunchRequest lr;
  private String result;

  public LaunchRequestHandler(LaunchRequest lr) {
    this.lr = lr;
  }

  @Override
  public void run() {
    result = handle();
  }

  public String getResult() {
    return result;
  }

  private String handle() {
    if (lr.mode == null) {
      return UNRECOGNIZED_MODE;
    }

    final ModuleManagerWindow window = ModuleManagerWindow.getInstance();

    switch (lr.mode) {
    case MANAGE:
      window.toFront();
      break;
    case LOAD:
      if (Player.LaunchAction.isEditing(lr.module)) {
        return MODULE_OPEN_FOR_EDITING;
      }

      if (lr.module == null && lr.game != null) {
        // attempt to find the module for the saved game or log
        final AbstractMetaData data = MetaDataFactory.buildMetaData(lr.game);
        if (data instanceof SaveMetaData) {
          // we found save metadata
          final String moduleName = ((SaveMetaData) data).getModuleName();
          if (moduleName != null && moduleName.length() > 0) {
            // get the module file by module name
            lr.module = window.getModuleByName(moduleName);
          }
          else {
            // this is a pre 3.1 save file, can't tell the module name
            return MODULE_NOT_FOUND;
          }
        }
      }

      if (lr.module == null) {
        return MODULE_NOT_FOUND;
      }
      else if (lr.game == null) {
        new Player.LaunchAction(window, lr.module).actionPerformed(null);
      }
      else {
        new Player.LaunchAction(window, lr.module, lr.game).actionPerformed(null);
      }
      break;
    case EDIT:
      if (Editor.LaunchAction.isInUse(lr.module)) {
        return MODULE_OPEN_FOR_PLAY;
      }

      if (Editor.LaunchAction.isEditing(lr.module)) {
        return MODULE_OPEN_FOR_EDITING;
      }

      new Editor.LaunchAction(window, lr.module).actionPerformed(null);
      break;
    case IMPORT:
      new Editor.ImportLaunchAction(window, lr.importFile).actionPerformed(null);
      break;
    case NEW:
      new Editor.NewModuleLaunchAction(window).actionPerformed(null);
      break;
    case EDIT_EXT:
      if (AbstractLaunchAction.isInUse(lr.module) ||
          AbstractLaunchAction.isInUse(lr.extension)) {
        return EXTENSION_OPEN;
      }

      new EditExtensionRequestLaunchAction(window, lr).actionPerformed(null);
      break;
    case NEW_EXT:
      if (AbstractLaunchAction.isEditing(lr.module)) {
        return MODULE_OPEN_FOR_EDITING;
      }

      new NewExtensionRequestLaunchAction(window, lr).actionPerformed(null);
      break;
    case UPDATE_MOD:
      window.updateRequest(lr.module);
      break;
    case UPDATE_EXT:
      window.updateRequest(lr.extension);
      break;
    case UPDATE_GAME:
      window.updateRequest(lr.game);
      break;
    default:
      return UNRECOGNIZED_MODE;
    }

    return null;
  }

  private abstract static class EditorRequestLaunchAction extends AbstractLaunchAction {
    private static final long serialVersionUID = 1L;

    protected EditorRequestLaunchAction(ModuleManagerWindow window, LaunchRequest lr) {
      super(lr.mode.toString(), window, Editor.class.getName(), lr);
    }
  }

  private static final class NewExtensionRequestLaunchAction extends EditorRequestLaunchAction {
    private static final long serialVersionUID = 1L;

    private NewExtensionRequestLaunchAction(ModuleManagerWindow window, LaunchRequest lr) {
      super(window, lr);
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
      incrementUsed(lr.module);
      super.actionPerformed(e);
    }

    @Override
    protected LaunchTask getLaunchTask() {
      return new LaunchTask() {
        @Override
        protected void done() {
          super.done();
          decrementUsed(lr.module);
        }
      };
    }
  }

  private static final class EditExtensionRequestLaunchAction extends EditorRequestLaunchAction {
    private static final long serialVersionUID = 1L;

    private EditExtensionRequestLaunchAction(ModuleManagerWindow window, LaunchRequest lr) {
      super(window, lr);
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
      incrementUsed(lr.module);
      markEditing(lr.extension);
      super.actionPerformed(e);
    }

    @Override
    protected LaunchTask getLaunchTask() {
      return new LaunchTask() {
        @Override
        protected void done() {
          super.done();
          decrementUsed(lr.module);
          unmarkEditing(lr.extension);
        }
      };
    }
  }
}
