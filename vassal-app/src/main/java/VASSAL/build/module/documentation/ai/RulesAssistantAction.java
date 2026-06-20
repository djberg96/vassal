/*
 * Copyright (c) 2026 by the Vassal developers
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 */
package VASSAL.build.module.documentation.ai;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import VASSAL.build.GameModule;
import VASSAL.i18n.Resources;

public class RulesAssistantAction extends AbstractAction {
  private static final long serialVersionUID = 1L;

  private RulesAssistantDialog dialog;

  public RulesAssistantAction() {
    super(Resources.getString("RulesAssistant.menu_item"));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final GameModule module = GameModule.getGameModule();
    if (dialog == null || !dialog.isDisplayable()) {
      dialog = new RulesAssistantDialog(
        module.getPlayerWindow(),
        new RulesAssistantService(module)
      );
    }

    dialog.setVisible(true);
    dialog.toFront();
  }
}
