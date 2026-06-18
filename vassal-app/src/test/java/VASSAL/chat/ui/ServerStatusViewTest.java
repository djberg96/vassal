/*
 * Copyright 2026 Vassal Development Team
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

package VASSAL.chat.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import VASSAL.chat.ServerStatus;
import org.junit.jupiter.api.Test;

import javax.swing.JTree;
import java.awt.Component;
import java.awt.Container;

public class ServerStatusViewTest {
  @Test
  public void nullStatusCreatesDisabledCurrentTab() {
    final ServerStatusView view = new ServerStatusView(null);

    assertFalse(view.isEnabled());
    assertEquals(1, view.getTabCount());
  }

  @Test
  public void statusCreatesCurrentAndHistoricalTabs() {
    final ServerStatusView view = new ServerStatusView(new EmptyServerStatus());

    assertTrue(view.isEnabled());
    assertEquals(3, view.getTabCount());
  }

  @Test
  public void serverStatusTreesUseRendererBasedFixedRowHeight() {
    final ServerStatusView view = new ServerStatusView(new EmptyServerStatus());
    final JTree tree = findTree(view);

    assertNotNull(tree);
    assertTrue(tree.isLargeModel());
    assertEquals(ServerStatusView.fixedTreeRowHeight(tree), tree.getRowHeight());
  }

  private static final class EmptyServerStatus implements ServerStatus {
    @Override
    public ModuleSummary[] getStatus() {
      return new ModuleSummary[0];
    }

    @Override
    public String[] getSupportedTimeRanges() {
      return new String[] {"1 hour", "1 day"};
    }

    @Override
    public ModuleSummary[] getHistory(String timeRange) {
      return new ModuleSummary[0];
    }
  }

  private static JTree findTree(Component component) {
    if (component instanceof JTree) {
      return (JTree) component;
    }
    if (component instanceof Container) {
      for (final Component child : ((Container) component).getComponents()) {
        final JTree tree = findTree(child);
        if (tree != null) {
          return tree;
        }
      }
    }
    return null;
  }
}
