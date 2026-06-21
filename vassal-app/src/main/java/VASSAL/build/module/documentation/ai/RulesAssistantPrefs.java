/*
 * Copyright (c) 2026 by the Vassal developers
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 */
package VASSAL.build.module.documentation.ai;

import VASSAL.configure.BooleanConfigurer;
import VASSAL.configure.PasswordConfigurer;
import VASSAL.configure.StringEnumConfigurer;
import VASSAL.configure.StringConfigurer;
import VASSAL.i18n.Resources;
import VASSAL.preferences.Prefs;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public final class RulesAssistantPrefs {
  public static final String ENABLED = "rulesAssistantEnabled"; //NON-NLS
  public static final String PROVIDER = "rulesAssistantProvider"; //NON-NLS
  public static final String MODEL = "rulesAssistantModel"; //NON-NLS
  public static final String API_KEY = "rulesAssistantApiKey"; //NON-NLS
  public static final String MANUS_TASK_ID_PREFIX = "rulesAssistantManusTaskId"; //NON-NLS

  public static final String OPENAI_PROVIDER = "OpenAI"; //NON-NLS
  public static final String XAI_PROVIDER = "xAI"; //NON-NLS
  public static final String MANUS_PROVIDER = "Manus"; //NON-NLS
  public static final String DEFAULT_PROVIDER = OPENAI_PROVIDER;

  public static final String OPENAI_BASE_URL = "https://api.openai.com/v1"; //NON-NLS
  public static final String XAI_BASE_URL = "https://api.x.ai/v1"; //NON-NLS
  public static final String MANUS_BASE_URL = "https://api.manus.ai"; //NON-NLS

  public static final String DEFAULT_OPENAI_MODEL = "gpt-5-mini"; //NON-NLS
  public static final String DEFAULT_XAI_MODEL = "grok-4.3"; //NON-NLS
  public static final String DEFAULT_MANUS_MODEL = "manus-1.6-lite"; //NON-NLS

  private RulesAssistantPrefs() {
  }

  public static void addGlobalPreferences(Prefs prefs) {
    final String tab = Resources.getString("Prefs.rules_assistant_tab");

    prefs.addOption(tab, new BooleanConfigurer(
      ENABLED,
      Resources.getString("Prefs.rules_assistant_enabled"),
      Boolean.FALSE
    ));

    prefs.addOption(tab, new StringEnumConfigurer(
      PROVIDER,
      Resources.getString("Prefs.rules_assistant_provider"),
      new String[] {OPENAI_PROVIDER, XAI_PROVIDER, MANUS_PROVIDER}
    ));

    prefs.addOption(tab, new StringConfigurer(
      MODEL,
      Resources.getString("Prefs.rules_assistant_model"),
      ""
    ));

    prefs.addOption(tab, new ApiKeyConfigurer(
      API_KEY,
      Resources.getString("Prefs.rules_assistant_api_key"),
      ""
    ));
  }

  public static boolean isEnabled(Prefs prefs) {
    return Boolean.TRUE.equals(prefs.getValue(ENABLED));
  }

  public static String getModel(Prefs prefs) {
    final Object value = prefs.getValue(MODEL);
    if (value instanceof String model && !model.isBlank()) {
      if (XAI_PROVIDER.equals(getProvider(prefs)) && DEFAULT_OPENAI_MODEL.equals(model.strip())) {
        return DEFAULT_XAI_MODEL;
      }
      if (MANUS_PROVIDER.equals(getProvider(prefs)) && DEFAULT_OPENAI_MODEL.equals(model.strip())) {
        return DEFAULT_MANUS_MODEL;
      }
      return model.strip();
    }
    return switch (getProvider(prefs)) {
    case XAI_PROVIDER -> DEFAULT_XAI_MODEL;
    case MANUS_PROVIDER -> DEFAULT_MANUS_MODEL;
    default -> DEFAULT_OPENAI_MODEL;
    };
  }

  public static String getProvider(Prefs prefs) {
    final Object value = prefs.getValue(PROVIDER);
    if (value instanceof String provider && XAI_PROVIDER.equals(provider)) {
      return XAI_PROVIDER;
    }
    if (value instanceof String provider && MANUS_PROVIDER.equals(provider)) {
      return MANUS_PROVIDER;
    }
    return DEFAULT_PROVIDER;
  }

  public static String getBaseUrl(Prefs prefs) {
    return switch (getProvider(prefs)) {
    case XAI_PROVIDER -> XAI_BASE_URL;
    case MANUS_PROVIDER -> MANUS_BASE_URL;
    default -> OPENAI_BASE_URL;
    };
  }

  public static String getProviderDisplayName(Prefs prefs) {
    return getProvider(prefs);
  }

  public static String getApiKey(Prefs prefs) {
    final Object value = prefs.getValue(API_KEY);
    if (value instanceof String apiKey) {
      return apiKey.strip();
    }
    return "";
  }

  static final class ApiKeyConfigurer extends PasswordConfigurer {
    private static final int API_KEY_COLUMNS = 32;

    private Component controls;
    private JButton showButton;
    private char maskedEchoChar;

    ApiKeyConfigurer(String key, String name, String val) {
      super(key, name, strip(val));
    }

    @Override
    protected JTextField buildTextField() {
      return new JPasswordField(API_KEY_COLUMNS);
    }

    @Override
    public String getValueString() {
      return strip(super.getValueString());
    }

    @Override
    public void setValue(String s) {
      super.setValue(strip(s));
    }

    @Override
    public Component getControls() {
      if (controls != null) {
        return controls;
      }

      controls = super.getControls();
      final Component passwordControl = p.getComponent(p.getComponentCount() - 1);
      p.remove(passwordControl);

      final JPasswordField passwordField = (JPasswordField) nameField;
      maskedEchoChar = passwordField.getEchoChar();

      showButton = new JButton(Resources.getString("Prefs.api_key_show"));
      showButton.addActionListener(e -> toggleKeyVisibility(passwordField));

      final JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
      inputRow.setOpaque(false);
      inputRow.add(passwordControl);
      inputRow.add(showButton);
      p.add(inputRow, "growx"); //NON-NLS
      return controls;
    }

    private void toggleKeyVisibility(JPasswordField passwordField) {
      final boolean hidden = passwordField.getEchoChar() != 0;
      passwordField.setEchoChar(hidden ? (char) 0 : maskedEchoChar);
      showButton.setText(Resources.getString(hidden ? "Prefs.api_key_hide" : "Prefs.api_key_show"));
    }

    private static String strip(String value) {
      return value == null ? "" : value.strip();
    }
  }
}
