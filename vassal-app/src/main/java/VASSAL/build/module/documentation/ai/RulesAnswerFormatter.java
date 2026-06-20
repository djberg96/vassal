/*
 * Copyright (c) 2026 by the Vassal developers
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 */
package VASSAL.build.module.documentation.ai;

import java.util.ArrayList;
import java.util.List;

final class RulesAnswerFormatter {
  private RulesAnswerFormatter() {
  }

  static String toHtml(String answer) {
    final StringBuilder body = new StringBuilder();
    final StringBuilder paragraph = new StringBuilder();
    final List<String> table = new ArrayList<>();
    boolean inList = false;

    for (final String rawLine : answer.split("\\R", -1)) { //NON-NLS
      final String line = rawLine.strip();

      if (line.isEmpty()) {
        appendParagraph(body, paragraph);
        inList = closeList(body, inList);
        appendTable(body, table);
        continue;
      }

      if (isTableLine(line)) {
        appendParagraph(body, paragraph);
        inList = closeList(body, inList);
        table.add(line);
        continue;
      }

      appendTable(body, table);

      if (line.startsWith("#")) { //NON-NLS
        appendParagraph(body, paragraph);
        inList = closeList(body, inList);
        appendHeading(body, line);
        continue;
      }

      if (line.startsWith("* ") || line.startsWith("- ")) { //NON-NLS
        appendParagraph(body, paragraph);
        if (!inList) {
          body.append("<ul>"); //NON-NLS
          inList = true;
        }
        body.append("<li>").append(formatInline(line.substring(2).strip())).append("</li>"); //NON-NLS
        continue;
      }

      if (!paragraph.isEmpty()) {
        paragraph.append(' ');
      }
      paragraph.append(line);
    }

    appendParagraph(body, paragraph);
    closeList(body, inList);
    appendTable(body, table);

    return "<html><head><style>" //NON-NLS
      + "body { font-family: sans-serif; font-size: 12pt; margin: 8px; line-height: 1.35; }" //NON-NLS
      + "p { margin: 0 0 10px 0; }" //NON-NLS
      + "ul { margin: 0 0 10px 24px; padding: 0; }" //NON-NLS
      + "li { margin: 0 0 7px 0; }" //NON-NLS
      + "h1, h2, h3, h4 { margin: 10px 0 6px 0; }" //NON-NLS
      + "table { border-collapse: collapse; margin: 0 0 10px 0; }" //NON-NLS
      + "th, td { border: 1px solid #bbb; padding: 3px 6px; vertical-align: top; }" //NON-NLS
      + "th { background: #eee; }" //NON-NLS
      + "</style></head><body>" //NON-NLS
      + body
      + "</body></html>"; //NON-NLS
  }

  private static void appendParagraph(StringBuilder body, StringBuilder paragraph) {
    if (paragraph.isEmpty()) {
      return;
    }
    body.append("<p>").append(formatInline(paragraph.toString())).append("</p>"); //NON-NLS
    paragraph.setLength(0);
  }

  private static boolean closeList(StringBuilder body, boolean inList) {
    if (inList) {
      body.append("</ul>"); //NON-NLS
    }
    return false;
  }

  private static void appendHeading(StringBuilder body, String line) {
    int level = 0;
    while (level < line.length() && line.charAt(level) == '#') {
      level++;
    }

    final int htmlLevel = Math.clamp(level, 1, 4);
    body.append("<h").append(htmlLevel).append('>') //NON-NLS
      .append(formatInline(line.substring(level).strip()))
      .append("</h").append(htmlLevel).append('>'); //NON-NLS
  }

  private static void appendTable(StringBuilder body, List<String> table) {
    if (table.isEmpty()) {
      return;
    }

    body.append("<table>"); //NON-NLS
    boolean header = true;
    for (final String row : table) {
      if (isTableSeparator(row)) {
        continue;
      }

      body.append("<tr>"); //NON-NLS
      final String tag = header ? "th" : "td"; //NON-NLS
      for (final String cell : tableCells(row)) {
        body.append('<').append(tag).append('>')
          .append(formatInline(cell.strip()))
          .append("</").append(tag).append('>'); //NON-NLS
      }
      body.append("</tr>"); //NON-NLS
      header = false;
    }
    body.append("</table>"); //NON-NLS
    table.clear();
  }

  private static boolean isTableLine(String line) {
    return line.startsWith("|") && line.endsWith("|") && line.indexOf('|', 1) > 0; //NON-NLS
  }

  private static boolean isTableSeparator(String line) {
    return line.replace("|", "") //NON-NLS
      .replace(":", "") //NON-NLS
      .replace("-", "") //NON-NLS
      .isBlank();
  }

  private static List<String> tableCells(String line) {
    final String normalized = line.substring(1, line.length() - 1);
    return List.of(normalized.split("\\|", -1)); //NON-NLS
  }

  private static String formatInline(String text) {
    return escapeHtml(text)
      .replaceAll("\\*\\*([^*]+)\\*\\*", "<b>$1</b>") //NON-NLS
      .replaceAll("(?<!\\*)\\*([^*]+)\\*(?!\\*)", "<i>$1</i>"); //NON-NLS
  }

  private static String escapeHtml(String text) {
    final StringBuilder sb = new StringBuilder(text.length());
    for (int i = 0; i < text.length(); i++) {
      switch (text.charAt(i)) {
      case '&' -> sb.append("&amp;"); //NON-NLS
      case '<' -> sb.append("&lt;"); //NON-NLS
      case '>' -> sb.append("&gt;"); //NON-NLS
      case '"' -> sb.append("&quot;"); //NON-NLS
      default -> sb.append(text.charAt(i));
      }
    }
    return sb.toString();
  }
}
