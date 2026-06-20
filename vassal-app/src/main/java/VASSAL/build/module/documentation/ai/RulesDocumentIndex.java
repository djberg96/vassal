/*
 * Copyright (c) 2026 by the Vassal developers
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 */
package VASSAL.build.module.documentation.ai;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.BrowserPDFFile;
import VASSAL.build.widget.Chart;
import VASSAL.build.widget.HtmlChart;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;

public class RulesDocumentIndex {
  private static final int MAX_CHUNKS = 8;
  private static final int MIN_TOKEN_LENGTH = 3;
  private static final Pattern TOKEN_SPLIT = Pattern.compile("[^\\p{IsAlphabetic}\\p{IsDigit}]+"); //NON-NLS

  private final List<RulesChunk> chunks;

  public RulesDocumentIndex(List<RulesChunk> chunks) {
    this.chunks = List.copyOf(chunks);
  }

  public static RulesDocumentIndex build(GameModule module) throws IOException {
    final List<RulesChunk> chunks = new ArrayList<>();

    for (final BrowserPDFFile pdf : module.getAllDescendantComponentsOf(BrowserPDFFile.class)) {
      final String title = pdf.getConfigureName();
      final String fileName = pdf.getAttributeValueString(BrowserPDFFile.PDF_FILE);
      if (fileName == null || fileName.isBlank()) {
        continue;
      }

      try (InputStream in = module.getDataArchive().getInputStream(fileName);
           PDDocument doc = Loader.loadPDF(in.readAllBytes())) {
        final PDFTextStripper stripper = new PDFTextStripper();
        for (int page = 1; page <= doc.getNumberOfPages(); page++) {
          stripper.setStartPage(page);
          stripper.setEndPage(page);
          final String text = normalizeText(stripper.getText(doc));
          if (!text.isBlank()) {
            chunks.add(new RulesChunk(title, fileName, page, text));
          }
        }
      }
    }

    for (final HtmlChart chart : module.getAllDescendantComponentsOf(HtmlChart.class)) {
      final String fileName = chart.getFileName();
      if (fileName == null || fileName.isBlank()) {
        continue;
      }

      final String localizedFileName = module.getResourcePathFinder().findHelpFileName(fileName);
      try (InputStream in = module.getDataArchive().getInputStream(localizedFileName)) {
        final String text = normalizeText(Jsoup.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8)).text());
        if (!text.isBlank()) {
          chunks.add(new RulesChunk(chartTitle(chart), localizedFileName, 0, text));
        }
      }
    }

    for (final Chart chart : module.getAllDescendantComponentsOf(Chart.class)) {
      final String text = normalizeText(chartText(chart));
      if (!text.isBlank()) {
        chunks.add(new RulesChunk(chartTitle(chart), chart.getFileName(), 0, text));
      }
    }

    return new RulesDocumentIndex(chunks);
  }

  public boolean isEmpty() {
    return chunks.isEmpty();
  }

  public List<RulesChunk> relevantChunks(String question) {
    final Set<String> queryTokens = tokenize(question);
    if (queryTokens.isEmpty()) {
      return chunks.stream().limit(MAX_CHUNKS).toList();
    }

    return chunks.stream()
      .map(chunk -> new ScoredChunk(chunk, score(chunk.text(), queryTokens)))
      .filter(scored -> scored.score() > 0)
      .sorted(Comparator.comparingInt(ScoredChunk::score).reversed())
      .limit(MAX_CHUNKS)
      .map(ScoredChunk::chunk)
      .toList();
  }

  private static int score(String text, Set<String> queryTokens) {
    final Set<String> chunkTokens = tokenize(text);
    int score = 0;
    for (final String token : queryTokens) {
      if (chunkTokens.contains(token)) {
        score++;
      }
    }
    return score;
  }

  static Set<String> tokenize(String text) {
    final Set<String> tokens = new HashSet<>();
    for (final String raw : TOKEN_SPLIT.split(text.toLowerCase(Locale.ROOT))) {
      if (raw.length() >= MIN_TOKEN_LENGTH) {
        tokens.add(raw);
      }
    }
    return tokens;
  }

  static String normalizeText(String text) {
    return text.replace('\u00a0', ' ')
      .replaceAll("[ \\t\\x0B\\f\\r]+", " ") //NON-NLS
      .replaceAll("\\n{3,}", "\n\n") //NON-NLS
      .strip();
  }

  static String chartText(Chart chart) {
    final List<String> parts = new ArrayList<>();
    final String name = chart.getConfigureName();
    if (name != null && !name.isBlank()) {
      parts.add("Chart name: " + name); //NON-NLS
    }

    final String description = chart.getDescription();
    if (description != null && !description.isBlank()) {
      parts.add("Description: " + description); //NON-NLS
    }

    final String fileName = chart.getFileName();
    if (fileName != null && !fileName.isBlank()) {
      parts.add("Image file: " + fileName); //NON-NLS
    }

    if (!parts.isEmpty()) {
      parts.add("This is an image chart. Its visual contents are not available as searchable text yet."); //NON-NLS
    }
    return String.join("\n", parts); //NON-NLS
  }

  private static String chartTitle(Chart chart) {
    return chartTitle(chart.getConfigureName());
  }

  private static String chartTitle(HtmlChart chart) {
    return chartTitle(chart.getConfigureName());
  }

  private static String chartTitle(String name) {
    if (name == null || name.isBlank()) {
      return "Chart"; //NON-NLS
    }
    return "Chart: " + name.strip(); //NON-NLS
  }

  private record ScoredChunk(RulesChunk chunk, int score) {
  }
}
