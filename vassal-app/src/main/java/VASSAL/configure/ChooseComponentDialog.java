/*
 *
 * Copyright (c) 2000-2003 by Rodney Kinney
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
package VASSAL.configure;

import VASSAL.build.Buildable;
import VASSAL.build.Configurable;
import VASSAL.build.GameModule;
import VASSAL.i18n.Resources;
import VASSAL.tools.ScrollPane;
import VASSAL.tools.swing.SwingUtils;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Dialog that prompts the user to select a component from the {@link ConfigureTree}
 */
public final class ChooseComponentDialog extends JDialog {
  private static final long serialVersionUID = 1L;

  private transient Configurable target;
  private final Class<? extends Buildable> targetClass;
  private final transient Predicate<Object> additionalTargetMatcher;
  private final boolean trackPath;
  private final JButton okButton;
  private final ConfigureTree tree;
  private transient Configurable[] path;

  public ChooseComponentDialog(Frame owner, Class<? extends Buildable> targetClass) {
    this(owner, targetClass, selected -> false, false);
  }

  public static ChooseComponentDialog withPath(Frame owner, Class<? extends Buildable> targetClass) {
    return new ChooseComponentDialog(owner, targetClass, selected -> false, true);
  }

  public static ChooseComponentDialog withPath(
    Frame owner,
    Class<? extends Buildable> targetClass,
    Predicate<Object> additionalTargetMatcher
  ) {
    return new ChooseComponentDialog(owner, targetClass, additionalTargetMatcher, true);
  }

  private ChooseComponentDialog(
    Frame owner,
    Class<? extends Buildable> targetClass,
    Predicate<Object> additionalTargetMatcher,
    boolean trackPath
  ) {
    super(owner, true);
    this.targetClass = targetClass;
    this.additionalTargetMatcher = additionalTargetMatcher;
    this.trackPath = trackPath;
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
    tree = new ConfigureTree(GameModule.getGameModule(), null, null, true) {
      private static final long serialVersionUID = 1L;

      @Override
      public void mousePressed(MouseEvent e) {
      }

      @Override
      public void mouseReleased(MouseEvent e) {
      }

      @Override
      protected Action buildEditAction(Configurable target) {
        return null;
      }
    };
    tree.addTreeSelectionListener(e -> updateSelection());
    add(new ScrollPane(tree));
    final Box b = Box.createHorizontalBox();
    okButton = new JButton(Resources.getString("General.ok"));
    okButton.setEnabled(false);
    okButton.addActionListener(e -> dispose());
    final JButton cancelButton = new JButton(Resources.getString("General.cancel"));
    cancelButton.addActionListener(e -> {
      target = null;
      dispose();
    });
    b.add(okButton);
    b.add(cancelButton);
    add(b);

    // Default actions on Enter/ESC
    SwingUtils.setDefaultButtons(getRootPane(), okButton, cancelButton);

    pack();
  }

  private void updateSelection() {
    boolean enabled = false;
    target = null;
    path = null;
    final TreePath path = tree.getSelectionPath();
    if (path != null) {
      final Object selected = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
      enabled = isValidTarget(selected);
      if (enabled) {
        target = (Configurable) selected;
        if (trackPath) {
          updatePath(path);
        }
      }
    }
    okButton.setEnabled(enabled);
  }

  private void updatePath(TreePath p) {
    final DefaultMutableTreeNode node = (DefaultMutableTreeNode) p.getLastPathComponent();
    final Object[] userObjects = node.getUserObjectPath();
    final Configurable[] selectedPath = new Configurable[userObjects.length];

    for (int i = 0; i < userObjects.length; i++) {
      selectedPath[i] = (Configurable) userObjects[i];
    }

    path = Arrays.copyOfRange(selectedPath, 1, selectedPath.length);
  }

  private boolean isValidTarget(Object selected) {
    return targetClass.isInstance(selected) || additionalTargetMatcher.test(selected);
  }

  public Configurable getTarget() {
    return target;
  }

  public Configurable[] getPath() {
    return path;
  }
}
