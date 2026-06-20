/*
 * Copyright (c) 2026 by the Vassal developers
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 */
package VASSAL.build.module.documentation.ai;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;

import VASSAL.i18n.Resources;

import net.miginfocom.swing.MigLayout;

public class RulesAssistantDialog extends JDialog {
  private static final long serialVersionUID = 1L;

  private final RulesAssistantService service;
  private final JTextArea questionArea = new JTextArea(4, 58);
  private final JTextPane answerArea = new JTextPane();
  private final JButton askButton = new JButton();

  public RulesAssistantDialog(Frame owner, RulesAssistantService service) {
    super(owner, Resources.getString("RulesAssistant.title"), false);
    this.service = service;
    buildUi();
    pack();
    setMinimumSize(new Dimension(640, 520));
    setLocationRelativeTo(owner);
  }

  private void buildUi() {
    questionArea.setLineWrap(true);
    questionArea.setWrapStyleWord(true);
    answerArea.setContentType("text/html"); //NON-NLS
    answerArea.setEditable(false);
    answerArea.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

    askButton.setAction(new AbstractAction(Resources.getString("RulesAssistant.ask")) {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        ask();
      }
    });

    final JPanel form = new JPanel(new MigLayout("ins dialog,fill", "[grow]", "[][grow][][grow][]")); //NON-NLS
    form.add(new JLabel(Resources.getString("RulesAssistant.question")), "wrap"); //NON-NLS
    form.add(new JScrollPane(questionArea), "grow,wrap"); //NON-NLS
    form.add(new JLabel(Resources.getString("RulesAssistant.answer")), "wrap"); //NON-NLS
    form.add(new JScrollPane(answerArea), "grow,wrap"); //NON-NLS
    form.add(askButton, "right"); //NON-NLS

    setLayout(new BorderLayout());
    add(form, BorderLayout.CENTER);
  }

  private void ask() {
    askButton.setEnabled(false);
    answerArea.setText(RulesAnswerFormatter.toHtml(Resources.getString("RulesAssistant.working")));

    new SwingWorker<String, Void>() {
      @Override
      protected String doInBackground() throws Exception {
        return service.ask(questionArea.getText());
      }

      @Override
      protected void done() {
        askButton.setEnabled(true);
        try {
          answerArea.setText(RulesAnswerFormatter.toHtml(get()));
          answerArea.setCaretPosition(0);
        }
        catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        catch (Exception e) {
          final Throwable cause = e.getCause() == null ? e : e.getCause();
          answerArea.setText(RulesAnswerFormatter.toHtml(""));
          JOptionPane.showMessageDialog(
            RulesAssistantDialog.this,
            cause.getMessage(),
            Resources.getString("RulesAssistant.title"),
            JOptionPane.ERROR_MESSAGE
          );
        }
      }
    }.execute();
  }
}
