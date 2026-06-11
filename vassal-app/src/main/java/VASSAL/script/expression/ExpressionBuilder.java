/*
 *
 * Copyright (c) 2008-2009 by Brent Easton
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
package VASSAL.script.expression;

import VASSAL.build.AbstractBuildable;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.configure.BeanShellExpressionConfigurer;
import VASSAL.configure.Configurer;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.PropertiesPieceFilter;
import VASSAL.i18n.Resources;
import VASSAL.tools.BrowserSupport;
import VASSAL.tools.ButtonFactory;
import VASSAL.tools.swing.SwingUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Function;

/**
 * Interactively build inline(beanshell) expressions
 */
public final class ExpressionBuilder extends JDialog {

  private static final long serialVersionUID = 1L;
  private transient BeanShellExpressionConfigurer expression;
  private transient Configurer target;
  private transient EditablePiece pieceTarget;
  private transient AbstractBuildable context;
  private final boolean propertyNameExpression;
  private final BeanShellExpressionConfigurer.Option expressionOption;

  public ExpressionBuilder(Configurer c, JDialog parent) {
    this(c, parent, null);
  }

  public ExpressionBuilder(Configurer c, JDialog parent, EditablePiece piece) {
    this(c, parent, piece, ExpressionBuilder::convertFormattedExpression, BeanShellExpressionConfigurer.Option.NONE, false);
  }

  public static ExpressionBuilder propertyExpression(Configurer c, JDialog parent, EditablePiece piece) {
    return new ExpressionBuilder(
      c,
      parent,
      piece,
      PropertiesPieceFilter::toBeanShellString,
      BeanShellExpressionConfigurer.Option.PME,
      false
    );
  }

  public static ExpressionBuilder propertyNameExpression(Configurer c, JDialog parent, EditablePiece piece) {
    return new ExpressionBuilder(
      c,
      parent,
      piece,
      BeanShellExpression::convertProperty,
      BeanShellExpressionConfigurer.Option.NONE,
      true
    );
  }

  private ExpressionBuilder(
    Configurer c,
    JDialog parent,
    EditablePiece piece,
    Function<String, String> converter,
    BeanShellExpressionConfigurer.Option expressionOption,
    boolean propertyNameExpression
  ) {
    super(parent, Resources.getString("Editor.ExpressionBuilder.component_type"), true);
    target = c;
    pieceTarget = piece;
    context = c.getContext();
    this.expressionOption = expressionOption;
    this.propertyNameExpression = propertyNameExpression;
    setLayout(new MigLayout("ins 0,filly", "[]", "[grow]rel[]"));
    final JPanel p = new JPanel(new MigLayout("wrap 1,filly", "[]", "[grow]rel[]")); //NON-NLS

    final String value = target.getValueString();

    if (value.startsWith("{") && value.endsWith("}")) {
      setExpression(value.substring(1, value.length() - 1));
    }
    else {
      setExpression(converter.apply(value));
    }

    p.add(expression.getControls(), "grow"); //NON-NLS

    final JPanel buttonBox = new JPanel(new MigLayout("", "[]rel[]rel[]")); //NON-NLS
    final JButton okButton = ButtonFactory.getOkButton();
    okButton.addActionListener(e -> save());
    buttonBox.add(okButton);

    final JButton cancelButton = ButtonFactory.getCancelButton();
    cancelButton.addActionListener(e -> cancel());
    buttonBox.add(cancelButton);

    final JButton helpButton = ButtonFactory.getHelpButton();
    helpButton.addActionListener(e -> BrowserSupport.openURL(HelpFile.getReferenceManualPage("ExpressionBuilder.html").getContents().toString())); //NON-NLS
    buttonBox.add(helpButton);

    p.add(buttonBox, "align center"); //NON-NLS
    add(p, "growy");

    // Default actions for Enter/ESC
    SwingUtils.setDefaultButtons(getRootPane(), okButton, cancelButton);

    pack();
    setLocationRelativeTo(getParent());
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent we) {
        cancel();
      }
    });
  }

  /**
   * OK button pressed. Set the expression back into the target configurer
   * as an inline expression.
   */
  public void save() {
    final String expr = expression.getValueString().trim();
    if (propertyNameExpression) {
      if (BeanShellExpression.isJavaIdentifier(expr)) {
        target.setValue(expr);
        dispose();
        return;
      }

      if (expr.startsWith("GetProperty(\"") && expr.endsWith("\")") && //NON-NLS
          (expr.length() - expr.replaceAll("\"", "").length()) == 2) {
        target.setValue(expr.substring(13, expr.length() - 2));
        dispose();
        return;
      }
    }

    if (expr.startsWith("{") && expr.endsWith("}")) {
      target.setValue(expr);
    }
    else {
      target.setValue("{" + expr + "}");
    }
    dispose();
  }

  public void cancel() {
    dispose();
  }

  /**
   * Convert an old-style $variable$ string to a BeanShell Expression
   * @param s Old-style string
   * @return expression
   */
  private static String convertFormattedExpression(String s) {
    return Expression.createExpression(s).toBeanShellString();
  }

  public void setExpression(String value) {
    if (expression == null) {
      final String prompt = target.getName().length() == 0 ? Resources.getString("Editor.ExpressionBuilder.expression") : target.getName();
      expression = new BeanShellExpressionConfigurer(null, prompt, value, pieceTarget);
    }
    expression.setValue(value);
    expression.setOption(expressionOption);
    expression.setContext(context);
    expression.setContextLevel(target.getContextLevel());
  }

  public String getExpression() {
    return expression == null ? "" : expression.getValueString();
  }

}
