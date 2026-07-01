/*
 * Copyright (c) 2026 by the Vassal developers
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 */
package VASSAL.build.module.documentation.ai;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

import VASSAL.build.GameModule;
import VASSAL.configure.StringConfigurer;
import VASSAL.preferences.Prefs;
import VASSAL.tools.jfr.JfrEvents;
import VASSAL.tools.jfr.RulesAssistantIndexEvent;
import VASSAL.tools.jfr.RulesAssistantRequestEvent;

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
    final RulesAssistantRequestEvent event = new RulesAssistantRequestEvent();
    event.moduleName = moduleName();
    event.questionLength = question == null ? 0 : question.length();
    event.begin();

    if (question == null || question.isBlank()) {
      JfrEvents.commitFailure(event, IOException.class.getName());
      throw new IOException("Enter a rules question first."); //NON-NLS
    }

    try {
      final Prefs prefs = Prefs.getGlobalPrefs();
      event.provider = RulesAssistantPrefs.getProviderDisplayName(prefs);
      if (!RulesAssistantPrefs.isEnabled(prefs)) {
        throw new IOException("Rules Assistant is disabled. Enable it in Preferences > Rules Assistant."); //NON-NLS
      }

      if (index == null) {
        index = buildIndex();
      }
      if (index.isEmpty()) {
        throw new IOException("This module has no rules or chart files for the Rules Assistant to read."); //NON-NLS
      }

      final List<RulesChunk> chunks = index.relevantChunks(question);
      event.chunkCount = chunks.size();
      if (chunks.isEmpty()) {
        throw new IOException("No relevant rules or chart excerpts were found in this module."); //NON-NLS
      }

      final String answer = clientFor(prefs).answer(buildPrompt(question, chunks));
      JfrEvents.markSuccess(event);
      return answer;
    }
    catch (IOException | RuntimeException e) {
      JfrEvents.markFailure(event, e);
      throw e;
    }
    finally {
      event.commit();
    }
  }

  private RulesDocumentIndex buildIndex() throws IOException {
    final RulesAssistantIndexEvent event = new RulesAssistantIndexEvent();
    event.moduleName = moduleName();
    event.begin();

    try {
      final RulesDocumentIndex builtIndex = RulesDocumentIndex.build(module);
      event.chunkCount = builtIndex.chunkCount();
      JfrEvents.markSuccess(event);
      return builtIndex;
    }
    catch (IOException | RuntimeException e) {
      JfrEvents.markFailure(event, e);
      throw e;
    }
    finally {
      event.commit();
    }
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
        taskTitle(),
        getHiddenPreference(prefs, manusTaskIdPreferenceKey(prefs)),
        taskId -> setHiddenPreference(prefs, manusTaskIdPreferenceKey(prefs), taskId)
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
    final String moduleName = moduleName();
    if (moduleName == null || moduleName.isBlank()) {
      return "VASSAL Rules Assistant"; //NON-NLS
    }
    return "VASSAL Rules Assistant - " + moduleName.strip(); //NON-NLS
  }

  private String moduleName() {
    return module.getLocalizedGameName();
  }

  private String manusTaskIdPreferenceKey(Prefs prefs) {
    final String moduleName = module.getGameName() == null ? "" : module.getGameName(); //NON-NLS
    final String moduleVersion = module.getGameVersion() == null ? "" : module.getGameVersion(); //NON-NLS
    final String keySeed = moduleName + '\n'
      + moduleVersion + '\n'
      + RulesAssistantPrefs.getBaseUrl(prefs) + '\n'
      + RulesAssistantPrefs.getModel(prefs) + '\n'
      + hash(RulesAssistantPrefs.getApiKey(prefs));
    return RulesAssistantPrefs.MANUS_TASK_ID_PREFIX + '.' + hash(keySeed).substring(0, 32);
  }

  private static String getHiddenPreference(Prefs prefs, String key) {
    ensureHiddenPreference(prefs, key);
    final Object value = prefs.getValue(key);
    return value instanceof String taskId ? taskId.strip() : ""; //NON-NLS
  }

  private static void setHiddenPreference(Prefs prefs, String key, String value) {
    ensureHiddenPreference(prefs, key);
    prefs.setValue(key, value == null ? "" : value.strip()); //NON-NLS
  }

  private static void ensureHiddenPreference(Prefs prefs, String key) {
    if (prefs.getOption(key) == null) {
      prefs.addOption(null, new StringConfigurer(key, null, "")); //NON-NLS
    }
  }

  private static String hash(String value) {
    try {
      final MessageDigest digest = MessageDigest.getInstance("SHA-256"); //NON-NLS
      return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    }
    catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  static String buildPrompt(String question, List<RulesChunk> chunks) {
    final StringBuilder prompt = new StringBuilder();
    prompt.append("Question:\n")
      .append(question.strip())
      .append("\n\nRules and chart excerpts:\n"); //NON-NLS

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

    prompt.append("\nAnswer with concise reasoning and cite source/page or chart labels."); //NON-NLS
    return prompt.toString();
  }
}
