/*
 *
 * Copyright (c) 2000-2013 by Rodney Kinney, Joel Uckelman, Brent Easton
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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import org.apache.commons.lang3.SystemUtils;

import VASSAL.Info;
import VASSAL.build.GameModule;
import VASSAL.build.module.ExtensionsLoader;
import VASSAL.build.module.ModuleExtension;
import VASSAL.build.module.WizardSupport;
import VASSAL.build.module.metadata.AbstractMetaData;
import VASSAL.build.module.metadata.MetaDataFactory;
import VASSAL.build.module.metadata.ModuleMetaData;
import VASSAL.i18n.Localization;
import VASSAL.i18n.Resources;
import VASSAL.preferences.Prefs;
import VASSAL.tools.DataArchive;
import VASSAL.tools.ErrorDialog;
import VASSAL.tools.JarArchive;
import VASSAL.tools.UsernameAndPasswordDialog;
import VASSAL.tools.WriteErrorDialog;
import VASSAL.tools.menu.MacOSXMenuManager;
import VASSAL.tools.menu.MenuBarProxy;
import VASSAL.tools.menu.MenuManager;

/**
 * @author Joel Uckelman
 * @since 3.1.0
 */
public final class Player extends Launcher {
  public static void main(String[] args) throws IOException {
    Info.setConfig(new StandardConfig());
    new Player(args);
  }

  private Player(String[] args) {
    super(args, createMenuManager());
  }

  private static MenuManager createMenuManager() {
    return SystemUtils.IS_OS_MAC ?
      new MacOSXMenuManager() : new PlayerMenuManager();
  }

  @Override
  protected void launch() throws IOException {
    if (lr.builtInModule) {
      GameModule.init(createModule(createDataArchive()));

      if (lr.autoext != null) {
        for (final String ext : lr.autoext) {
          createExtension(ext).build();
        }
      }

      createExtensionsLoader().addTo(GameModule.getGameModule());
      Localization.getInstance().translate();
      showWizardOrPlayerWindow(GameModule.getGameModule());
    }
    else {
      GameModule.init(createModule(createDataArchive()));
      createExtensionsLoader().addTo(GameModule.getGameModule());
      Localization.getInstance().translate();
      final GameModule m = GameModule.getGameModule();
      if (lr.game != null) {
        m.getPlayerWindow().setVisible(true);
        m.setGameFile(lr.game.getName(), GameModule.GameFileMode.LOADED_GAME);
        m.getGameState().loadGameInBackground(lr.game);
      }
      else {
        showWizardOrPlayerWindow(m);
      }
    }
  }

  protected ExtensionsLoader createExtensionsLoader() {
    return new ExtensionsLoader();
  }

  protected ModuleExtension createExtension(String name) {
    return new ModuleExtension(new JarArchive(name));
  }

  protected DataArchive createDataArchive() throws IOException {
    if (lr.builtInModule) {
      return new JarArchive();
    }
    else {
      return new DataArchive(lr.module.getPath());
    }
  }

  protected GameModule createModule(DataArchive archive) {
    return new GameModule(archive);
  }

  private void showWizardOrPlayerWindow(GameModule module) {
    module.getPlayerWindow().setVisible(true);

    final Boolean showWizard = (Boolean) Prefs.getGlobalPrefs().getValue(WizardSupport.WELCOME_WIZARD_KEY);
    if (Boolean.TRUE.equals(showWizard)) {
      module.getWizardSupport().showWelcomeWizard();
    }
    else {
      // prompt for username and password if wizard is off
      // but no username is set
      if (!module.isRealName()) {
        UsernameAndPasswordDialog.prompt(module.getPlayerWindow()).ifPresent(
          credentials -> saveCredentials(module, credentials)
        );
      }
    }
  }

  private void saveCredentials(
    GameModule module,
    UsernameAndPasswordDialog.Credentials credentials
  ) {
    final Prefs prefs = module.getPrefs();

    prefs.getOption(GameModule.REAL_NAME).setValue(credentials.getUsername());
    prefs.getOption(GameModule.SECRET_NAME).setValue(credentials.getPassword());

    try {
      prefs.save();
    }
    catch (IOException e) {
      WriteErrorDialog.error(e, prefs.getFile());
    }
  }

  private abstract static class AbstractPlayerLaunchAction extends AbstractLaunchAction {
    private static final long serialVersionUID = 1L;

    protected AbstractPlayerLaunchAction(String name, ModuleManagerWindow mm, LaunchRequest launchRequest) {
      super(name, mm, Player.class.getName(), launchRequest);
    }

    protected final boolean prepareLaunch() {
      if (isEditing(lr.module)) return false;

      // don't permit loading of VASL saved before 3.4
      final AbstractMetaData data = MetaDataFactory.buildMetaData(lr.module);
      if (data instanceof ModuleMetaData) {
        if (!checkModuleLoadable((ModuleMetaData)data)) {
          return false;
        }
      }
      else {
        if (lr.module != null) {
          // A module in the MM should be a valid Module, but people can and do delete
          // or replace module files while the MM is running.
          ErrorDialog.show("Error.invalid_vassal_module", lr.module.getAbsolutePath()); //NON-NLS
          lr.module = null;
        }
        return false;
      }

      // increase the using count
      incrementUsed(lr.module);
      return true;
    }

    @Override
    protected final LaunchTask getLaunchTask() {
      return new LaunchTask() {
        @Override
        protected void done() {
          super.done();

          // reduce the using count
          decrementUsed(lr.module);
        }
      };
    }
  }

  public static final class LaunchAction extends AbstractPlayerLaunchAction {
    private static final long serialVersionUID = 1L;

    public LaunchAction(ModuleManagerWindow mm, File module) {
      super(
        Resources.getString("Main.play_module_specific"),
        mm,
        new LaunchRequest(LaunchRequest.Mode.LOAD, module)
      );
      setEnabled(!isEditing(module));
    }

    public LaunchAction(ModuleManagerWindow mm, File module, File saveGame) {
      super(
        Resources.getString("General.open"),
        mm,
        new LaunchRequest(LaunchRequest.Mode.LOAD, module, saveGame)
      );
      setEnabled(!isEditing(module));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (!prepareLaunch()) return;
      super.actionPerformed(e);
    }
  }

  public static final class PromptLaunchAction extends AbstractPlayerLaunchAction {
    private static final long serialVersionUID = 1L;

    public PromptLaunchAction(ModuleManagerWindow mm) {
      super(
        Resources.getString("Main.play_module"),
        mm,
        new LaunchRequest(LaunchRequest.Mode.LOAD, null)
      );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      // prompt the user to pick a module
      if (promptForFile() == null) return;

      final AbstractMetaData data = MetaDataFactory.buildMetaData(lr.module);
      if (data != null && Info.isModuleTooNew(data.getVassalVersion())) {
        ErrorDialog.show(
          "Error.module_too_new", //NON-NLS
          lr.module.getPath(),
          data.getVassalVersion(),
          Info.getVersion()
        );
        return;
      }

      if (!prepareLaunch()) return;
      super.actionPerformed(e);
    }
  }

  private static class PlayerMenuManager extends MenuManager {
    private final MenuBarProxy menuBar = new MenuBarProxy();

    @Override
    public JMenuBar getMenuBarFor(JFrame fc) {
      return (fc instanceof PlayerWindow) ? menuBar.createPeer() : null;
    }

    @Override
    public MenuBarProxy getMenuBarProxyFor(JFrame fc) {
      return (fc instanceof PlayerWindow) ? menuBar : null;
    }
  }
}
