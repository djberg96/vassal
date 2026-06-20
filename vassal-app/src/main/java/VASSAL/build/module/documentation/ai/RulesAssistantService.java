/*
 * Copyright (c) 2026 by the Vassal developers
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 */
package VASSAL.build.module.documentation.ai;

import java.io.IOException;
import java.util.List;

import VASSAL.build.GameModule;
import VASSAL.preferences.Prefs;

public class RulesAssistantService {
  private static final int MAX_CONTEXT_CHARS = 18_000;

  private final GameModule module;
  private RulesDocumentIndex index;
  private RulesAssistantClient client;
  private String clientKey;

  public RulesAssistantService(GameModule module) {
    this.module = module;
  }

  public String ask(String question) throws IOException {
    if (question == null || question.isBlank()) {
      throw new IOException("Enter a rules question first."); //NON-NLS
    }

    final Prefs prefs = Prefs.getGlobalPrefs();
    if (!RulesAssistantPrefs.isEnabled(prefs)) {
      throw new IOException("Rules Assistant is disabled. Enable it in Preferences > Rules Assistant."); //NON-NLS
    }

    if (index == null) {
      index = RulesDocumentIndex.build(module);
    }
    if (index.isEmpty()) {
      throw new IOException("This module has no PDF help files for the Rules Assistant to read."); //NON-NLS
    }

    final List<RulesChunk> chunks = index.relevantChunks(question);
    if (chunks.isEmpty()) {
      throw new IOException("No relevant rules excerpts were found in this module's PDF help files."); //NON-NLS
    }

    return clientFor(prefs).answer(buildPrompt(question, chunks));
  }

  private RulesAssistantClient clientFor(Prefs prefs) {
    final String key = RulesAssistantPrefs.getProvider(prefs)
      + '\n' + RulesAssistantPrefs.getBaseUrl(prefs)
      + '\n' + RulesAssistantPrefs.getModel(prefs)
      + '\n' + RulesAssistantPrefs.getApiKey(prefs);
    if (client != null && key.equals(clientKey)) {
      return client;
    }

    clientKey = key;
    if (RulesAssistantPrefs.MANUS_PROVIDER.equals(RulesAssistantPrefs.getProvider(prefs))) {
      client = new ManusRulesAssistantClient(
        RulesAssistantPrefs.getApiKey(prefs),
        RulesAssistantPrefs.getModel(prefs),
        RulesAssistantPrefs.getBaseUrl(prefs),
        taskTitle()
      );
      return client;
    }

    client = new OpenAIRulesAssistantClient(
      RulesAssistantPrefs.getApiKey(prefs),
      RulesAssistantPrefs.getModel(prefs),
      RulesAssistantPrefs.getBaseUrl(prefs),
      RulesAssistantPrefs.getProviderDisplayName(prefs)
    );
    return client;
  }

  private String taskTitle() {
    final String moduleName = module.getLocalizedGameName();
    if (moduleName == null || moduleName.isBlank()) {
      return "VASSAL Rules Assistant"; //NON-NLS
    }
    return "VASSAL Rules Assistant - " + moduleName.strip(); //NON-NLS
  }

  static String buildPrompt(String question, List<RulesChunk> chunks) {
    final StringBuilder prompt = new StringBuilder();
    prompt.append("Question:\n")
      .append(question.strip())
      .append("\n\nRules excerpts:\n"); //NON-NLS

    int remaining = MAX_CONTEXT_CHARS;
    for (final RulesChunk chunk : chunks) {
      if (remaining <= 0) {
        break;
      }

      final String citation = chunk.citation();
      final String text = chunk.text();
      final int excerptLength = Math.min(text.length(), remaining);
      prompt.append("\n[")
        .append(citation)
        .append("]\n")
        .append(text, 0, excerptLength)
        .append('\n');
      remaining -= citation.length() + excerptLength;
    }

    prompt.append("\nAnswer with concise reasoning and cite source/page labels."); //NON-NLS
    return prompt.toString();
  }
}
