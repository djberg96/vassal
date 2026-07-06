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
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;

import VASSAL.build.GameModule;
import VASSAL.build.module.NotesWindow;
import VASSAL.i18n.Resources;
import VASSAL.tools.concurrent.BackgroundTasks;

import net.miginfocom.swing.MigLayout;

public class RulesAssistantDialog extends JDialog {
  private static final long serialVersionUID = 1L;

  private final RulesAssistantService service;
  private final NotesWindow notesWindow;
  private final List<HistoryEntry> history = new ArrayList<>();
  private final JTextArea questionArea = new JTextArea(3, 58);
  private final JTextPane answerArea = new JTextPane();
  private final JButton previousButton = new JButton();
  private final JButton nextButton = new JButton();
  private final JButton copyButton = new JButton();
  private final JButton saveToNotesButton = new JButton();
  private final JButton askButton = new JButton();
  private final JButton closeButton = new JButton();
  private Future<?> askTask;
  private int historyIndex = -1;
  private String currentAnswer = "";

  public RulesAssistantDialog(Frame owner, RulesAssistantService service) {
    super(owner, Resources.getString("RulesAssistant.title"), false);
    this.service = service;
    notesWindow = findNotesWindow();
    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
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

    previousButton.setAction(new AbstractAction(Resources.getString("RulesAssistant.previous")) {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        showHistory(historyIndex - 1);
      }
    });

    nextButton.setAction(new AbstractAction(Resources.getString("RulesAssistant.next")) {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        showHistory(historyIndex + 1);
      }
    });

    copyButton.setAction(new AbstractAction(Resources.getString("RulesAssistant.copy")) {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        copyAnswer();
      }
    });

    saveToNotesButton.setAction(new AbstractAction(Resources.getString("RulesAssistant.save_to_notes")) {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        saveToNotes();
      }
    });
    saveToNotesButton.setVisible(notesWindow != null);

    askButton.setAction(new AbstractAction(Resources.getString("RulesAssistant.ask")) {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        ask();
      }
    });

    closeButton.setAction(new AbstractAction(Resources.getString("RulesAssistant.close")) {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });

    final JPanel buttons = new JPanel(new MigLayout("ins 0,fillx", "[][][grow][][][][]", "[]")); //NON-NLS
    buttons.add(previousButton);
    buttons.add(nextButton);
    buttons.add(new JLabel(), "growx"); //NON-NLS
    buttons.add(copyButton);
    buttons.add(saveToNotesButton);
    buttons.add(askButton);
    buttons.add(closeButton);

    final JPanel form = new JPanel(new MigLayout("ins dialog,fill", "[grow]", "[][][][grow][]")); //NON-NLS
    form.add(new JLabel(Resources.getString("RulesAssistant.question")), "wrap"); //NON-NLS
    form.add(new JScrollPane(questionArea), "growx,wrap"); //NON-NLS
    form.add(new JLabel(Resources.getString("RulesAssistant.answer")), "wrap"); //NON-NLS
    form.add(new JScrollPane(answerArea), "grow,wrap"); //NON-NLS
    form.add(buttons, "right"); //NON-NLS

    setLayout(new BorderLayout());
    add(form, BorderLayout.CENTER);
    updateActionButtons();
  }

  private void ask() {
    if (askTask != null && !askTask.isDone()) {
      return;
    }

    final String question = questionArea.getText();
    askButton.setEnabled(false);
    currentAnswer = "";
    answerArea.setText(RulesAnswerFormatter.toHtml(Resources.getString("RulesAssistant.working")));
    updateActionButtons();

    askTask = BackgroundTasks.submitWithCallbacksOnEdt(
      "rules-assistant-ask", //NON-NLS
      () -> service.ask(question),
      answer -> {
        askButton.setEnabled(true);
        history.add(new HistoryEntry(question, answer));
        showHistory(history.size() - 1);
      },
      error -> {
        askButton.setEnabled(true);
        setAnswer("");
        JOptionPane.showMessageDialog(
          RulesAssistantDialog.this,
          error.getMessage(),
          Resources.getString("RulesAssistant.title"),
          JOptionPane.ERROR_MESSAGE
        );
      }
    );
  }

  private static NotesWindow findNotesWindow() {
    final GameModule module = GameModule.getGameModule();
    if (module == null) {
      return null;
    }

    final List<NotesWindow> notesWindows = module.getAllDescendantComponentsOf(NotesWindow.class);
    return notesWindows.isEmpty() ? null : notesWindows.get(0);
  }

  private void showHistory(int index) {
    if (index < 0 || index >= history.size()) {
      return;
    }

    historyIndex = index;
    final HistoryEntry entry = history.get(index);
    questionArea.setText(entry.question());
    setAnswer(entry.answer());
  }

  private void setAnswer(String answer) {
    currentAnswer = answer == null ? "" : answer;
    answerArea.setText(RulesAnswerFormatter.toHtml(currentAnswer));
    answerArea.setCaretPosition(0);
    updateActionButtons();
  }

  private void updateActionButtons() {
    previousButton.setEnabled(historyIndex > 0);
    nextButton.setEnabled(historyIndex >= 0 && historyIndex < history.size() - 1);
    copyButton.setEnabled(!currentAnswer.isBlank());
    saveToNotesButton.setEnabled(notesWindow != null && !currentAnswer.isBlank());
  }

  private void copyAnswer() {
    final String text = selectedOrCurrentAnswer();
    if (text.isBlank()) {
      return;
    }

    Toolkit.getDefaultToolkit()
      .getSystemClipboard()
      .setContents(new StringSelection(text), null);
  }

  private void saveToNotes() {
    if (notesWindow == null) {
      return;
    }

    final String text = selectedOrCurrentAnswer();
    if (text.isBlank()) {
      return;
    }

    notesWindow.appendPublicNote(formatNote(currentQuestion(), text, hasSelectedAnswerText()));
    JOptionPane.showMessageDialog(
      this,
      Resources.getString("RulesAssistant.saved_to_notes"),
      Resources.getString("RulesAssistant.title"),
      JOptionPane.INFORMATION_MESSAGE
    );
  }

  private boolean hasSelectedAnswerText() {
    final String selected = answerArea.getSelectedText();
    return selected != null && !selected.isBlank();
  }

  private String selectedOrCurrentAnswer() {
    final String selected = answerArea.getSelectedText();
    return selected != null && !selected.isBlank() ? selected : currentAnswer;
  }

  private String currentQuestion() {
    if (historyIndex >= 0 && historyIndex < history.size()) {
      return history.get(historyIndex).question();
    }

    return questionArea.getText();
  }

  private static String formatNote(String question, String answer, boolean excerpt) {
    return Resources.getString("RulesAssistant.title") + System.lineSeparator()
      + Resources.getString("RulesAssistant.question") + ": " + question.strip() + System.lineSeparator()
      + System.lineSeparator()
      + Resources.getString(excerpt ? "RulesAssistant.excerpt" : "RulesAssistant.answer") + ":"
      + System.lineSeparator()
      + answer.strip();
  }

  private record HistoryEntry(String question, String answer) {
  }
}
