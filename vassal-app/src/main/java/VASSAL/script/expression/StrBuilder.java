/*
 * $Id: StrBuilder.java 7725 2011-07-31 18:51:43Z uckelman $
 *
 * Copyright (c) 2008-2012 Brent Easton
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

/*
 * StrBuilder.
 * Build a string constant. Allow user to enter a string and clean it up, add " if necessary and
 * escape internal ".
 */

package VASSAL.script.expression;

import VASSAL.configure.StringConfigurer;
import VASSAL.i18n.Resources;
import VASSAL.tools.ButtonFactory;
import VASSAL.tools.swing.SwingUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class StrBuilder extends JDialog {
  private static final long serialVersionUID = 1L;
  private transient StringConfigurer target;
  private transient StringConfigurer entry;
  private final boolean integer;

  public StrBuilder(StringConfigurer c, JDialog parent) {
    this(
      c,
      parent,
      Resources.getString("Editor.StringBuilder.component_type"),
      Resources.getString("Editor.StringBuilder.object_type"),
      false
    );
  }

  public static StrBuilder integer(StringConfigurer c, JDialog parent) {
    return new StrBuilder(
      c,
      parent,
      Resources.getString("Editor.IntBuilder.component_type"),
      Resources.getString("Editor.IntBuilder.object_type"),
      true
    );
  }

  private StrBuilder(StringConfigurer c, JDialog parent, String title, String type, boolean integer) {
    super(parent, title, true);
    target = c;
    this.integer = integer;
    build(type);
  }

  protected void build(String type) {
    setLayout(new MigLayout("fillx")); //NON-NLS

    final JPanel p = new JPanel();
    p.setLayout(new MigLayout("fillx", "[grow 0]rel[grow 1]")); //NON-NLS

    p.add(new JLabel(type + ":"), "growx"); //NON-NLS
    entry = new StringConfigurer("", "");
    p.add(entry.getControls(), "wrap,growx"); //NON-NLS


    final JPanel buttonBox = new JPanel(new MigLayout("", "[]rel[]rel[]")); //NON-NLS
    final JButton okButton = ButtonFactory.getOkButton();
    okButton.addActionListener(e -> save());
    buttonBox.add(okButton);

    final JButton cancelButton = ButtonFactory.getCancelButton();
    cancelButton.addActionListener(e -> cancel());
    buttonBox.add(cancelButton);

    p.add(buttonBox, "span 2,align center"); //NON-NLS
    add(p, "growx"); //NON-NLS

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

  protected void save() {
    String result = entry.getValueString();
    if (integer) {
      result = integerValue(result);
      target.setValue(result);
      dispose();
      return;
    }

    if (result.startsWith("\"") && result.endsWith("\"")) {
      result = result.substring(1, result.length() - 1);
    }
    result = result.replace("\"", "\\\"");
    target.setValue("\"" + result + "\"");
    dispose();
  }

  private String integerValue(String value) {
    boolean negative = false;
    final StringBuilder result = new StringBuilder();

    if (value.startsWith("-")) {
      value = value.substring(1);
      negative = true;
    }

    for (int i = 0; i < value.length(); i++) {
      final char c = value.charAt(i);
      if (c >= '0' && c <= '9') {
        result.append(c);
      }
    }

    final String converted = (negative ? "-" : "") + result;
    if (converted.length() == 0 || converted.equals("-")) {
      return "0";
    }

    return converted;
  }

  protected void cancel() {
    dispose();
  }

}
