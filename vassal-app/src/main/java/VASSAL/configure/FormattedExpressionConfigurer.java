/*
 *
 * Copyright (c) 2008-2012 by Brent Easton
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

import VASSAL.build.module.properties.PropertyChangerConfigurer.Constraints;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.GamePiece;
import VASSAL.i18n.Resources;
import VASSAL.script.expression.ExpressionBuilder;
import VASSAL.tools.FormattedString;
import VASSAL.tools.icon.IconFactory;
import VASSAL.tools.icon.IconFamily;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * A standard Formatted String configurer that has an additional
 * Calculator icon that:
 *   a) Indicates to the user that $name$ variables can be used in this field
 *   b) Clicking on it opens up an Expression Builder that allows entry of
 *      in-line Calculated Properties (Not implemented yet)
 */
public class FormattedExpressionConfigurer extends FormattedStringConfigurer {
  protected ExpressionButton button;
  protected EditablePiece pieceTarget;

  public FormattedExpressionConfigurer(String key, String name) {
    super(key, name);
  }

  public FormattedExpressionConfigurer(String key, String name, String s) {
    super(key, name);
    value = s;
  }

  public FormattedExpressionConfigurer(String s) {
    this(null, "", s);
  }

  public FormattedExpressionConfigurer(String key, String name, FormattedString s) {
    this(key, name, s.getFormat());
  }

  public FormattedExpressionConfigurer(String key, String name, String s, EditablePiece p) {
    this(key, name, s, (GamePiece) p);
  }

  public FormattedExpressionConfigurer(String s, Constraints p) {
    this(null, "", s, p);
  }

  public FormattedExpressionConfigurer(String key, String name, String s, Constraints p) {
    this(key, name, s);
    if (p instanceof GamePiece) {
      pieceTarget = findEditablePiece((GamePiece) p);
    }
  }

  public FormattedExpressionConfigurer(String s, GamePiece p) {
    this(null, "", s, p);
  }

  public FormattedExpressionConfigurer(String key, String name, String s, GamePiece p) {
    this(key, name, s);
    pieceTarget = findEditablePiece(p);
  }

  protected final void storePiece(GamePiece p) {
    pieceTarget = findEditablePiece(p);
  }

  private static EditablePiece findEditablePiece(GamePiece p) {
    if (p instanceof Decorator) {
      final GamePiece gp = Decorator.getOutermost(p);
      if (gp instanceof EditablePiece) {
        return (EditablePiece) gp;
      }
    }
    return null;
  }

  public FormattedExpressionConfigurer(String key, String name, String[] options) {
    super(key, name, options);
  }

  public FormattedExpressionConfigurer(String[] options) {
    super(options);
  }

  @Override
  public Component getControls() {
    final JPanel p = (JPanel) super.getControls();
    if (button == null) {
      button = buildButton();
      p.add(button);
    }
    button.setSize(nameField.getPreferredSize().height);
    return p;
  }

  protected ExpressionButton buildButton() {
    return new ExpressionButton(this, nameField.getPreferredSize().height, pieceTarget);
  }

  /**
   * A small 'Calculator' button added after the text to indicate this
   * Configurer accepts Expressions. Clicking on the button will open
   * an ExpressionConfigurer.
   *
   */
  public static class ExpressionButton extends JButton implements ActionListener {
    private static final long serialVersionUID = 1L;
    protected transient Configurer config;
    protected transient EditablePiece piece;
    private Dimension buttonSize;
    private boolean actionListenerAdded;

    public ExpressionButton(Configurer config, int size) {
      this(config, size, null);
    }

    public ExpressionButton(Configurer config, int size, EditablePiece piece) {
      this.config = config;
      this.piece = piece;
      buttonSize = new Dimension(size, size);
    }

    public void setSize(int size) {
      setButtonSize(size);
    }

    private void setButtonSize(int size) {
      buttonSize = new Dimension(size, size);
      applyButtonSize();
    }

    private void applyButtonSize() {
      setPreferredSize(buttonSize);
      setMaximumSize(buttonSize);
    }

    @Override
    public void addNotify() {
      super.addNotify();
      applyButtonSize();
      setIcon(IconFactory.getIcon("calculator", IconFamily.XSMALL)); //NON-NLS
      setToolTipText(Resources.getString("Editor.FormattedExpressionConfigurer.expression_builder"));
      if (!actionListenerAdded) {
        addActionListener(this);
        actionListenerAdded = true;
      }
    }

    @Override
    public void removeNotify() {
      if (actionListenerAdded) {
        removeActionListener(this);
        actionListenerAdded = false;
      }
      super.removeNotify();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      new ExpressionBuilder(config, (JDialog) getTopLevelAncestor(), piece).setVisible(true);
    }
  }
}
