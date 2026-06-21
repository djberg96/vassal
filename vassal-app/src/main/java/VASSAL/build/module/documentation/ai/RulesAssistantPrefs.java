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
import VASSAL.i18n.Resources;
import VASSAL.preferences.Prefs;
import VASSAL.tools.concurrent.BackgroundTasks;
import VASSAL.tools.http.HttpClientService;

import java.awt.Component;
import java.awt.FlowLayout;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.swing.JButton;
import javax.swing.JOptionPane;
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
  static final String AUTOMATIC_MODEL = ""; //NON-NLS

  private static final String[] OPENAI_MODEL_DEFAULTS = { AUTOMATIC_MODEL, DEFAULT_OPENAI_MODEL };
  private static final String[] XAI_MODEL_DEFAULTS = { AUTOMATIC_MODEL, DEFAULT_XAI_MODEL, "grok-build-0.1" }; //NON-NLS
  private static final String[] MANUS_MODEL_DEFAULTS = { AUTOMATIC_MODEL, "manus-1.6", DEFAULT_MANUS_MODEL, "manus-1.6-max" }; //NON-NLS

  private RulesAssistantPrefs() {
  }

  public static void addGlobalPreferences(Prefs prefs) {
    final String tab = Resources.getString("Prefs.rules_assistant_tab");

    prefs.addOption(tab, new BooleanConfigurer(
      ENABLED,
      Resources.getString("Prefs.rules_assistant_enabled"),
      Boolean.FALSE
    ));

    final StringEnumConfigurer provider = new StringEnumConfigurer(
      PROVIDER,
      Resources.getString("Prefs.rules_assistant_provider"),
      new String[] {OPENAI_PROVIDER, XAI_PROVIDER, MANUS_PROVIDER}
    );
    prefs.addOption(tab, provider);

    prefs.addOption(tab, new ModelConfigurer(
      prefs,
      provider,
      MODEL,
      Resources.getString("Prefs.rules_assistant_model"),
      AUTOMATIC_MODEL
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

  static final class ModelConfigurer extends StringEnumConfigurer {
    private static final HttpClientService HTTP =
      HttpClientService.createDefault(Duration.ofSeconds(15));

    private final Prefs prefs;
    private final StringEnumConfigurer providerConfigurer;
    private boolean providerListenerInstalled;
    private JButton refreshButton;
    private Future<?> refreshTask;

    ModelConfigurer(Prefs prefs, StringEnumConfigurer providerConfigurer, String key, String name, String val) {
      super(key, name, OPENAI_MODEL_DEFAULTS);
      this.prefs = prefs;
      this.providerConfigurer = providerConfigurer;
      setValue(val);
    }

    @Override
    public Component getControls() {
      final Component controls = super.getControls();
      setEditable(true);
      installProviderListener();

      if (refreshButton == null) {
        refreshButton = new JButton(Resources.getString("Prefs.rules_assistant_refresh_models"));
        refreshButton.addActionListener(e -> refreshModels());
        if (controls instanceof JPanel panel) {
          panel.add(refreshButton);
        }
      }

      updateModelChoices(currentProvider(), true);
      return controls;
    }

    @Override
    public void setValue(Object o) {
      final String value = o == null ? "" : o.toString().strip(); //NON-NLS
      final Object oldValue = getValue();
      this.value = value;
      if (!frozen) {
        getChangeSupport().firePropertyChange(key, oldValue, value);
      }
      if (!noUpdate && getBox() != null) {
        getBox().setSelectedItem(value);
      }
    }

    @Override
    public String getValueString() {
      return getValue() == null ? "" : getValue().toString().strip(); //NON-NLS
    }

    @Override
    public void setValue(String s) {
      setValue((Object) s);
    }

    private void installProviderListener() {
      if (providerListenerInstalled) {
        return;
      }

      providerConfigurer.getControls();
      if (providerConfigurer.getBox() != null) {
        providerConfigurer.getBox().addActionListener(e -> updateModelChoices(currentProvider(), false));
        providerListenerInstalled = true;
      }
    }

    private void refreshModels() {
      if (refreshTask != null && !refreshTask.isDone()) {
        return;
      }

      final String provider = currentProvider();
      refreshButton.setEnabled(false);
      refreshTask = BackgroundTasks.submit(
        () -> refreshedModelIds(provider),
        modelIds -> {
          refreshButton.setEnabled(true);
          updateModelChoices(provider, modelIds, true);
          JOptionPane.showMessageDialog(
            refreshButton,
            Resources.getString("Prefs.rules_assistant_refresh_models_success"),
            Resources.getString("Prefs.rules_assistant_refresh_models"),
            JOptionPane.INFORMATION_MESSAGE
          );
        },
        error -> {
          refreshButton.setEnabled(true);
          JOptionPane.showMessageDialog(
            refreshButton,
            Resources.getString("Prefs.rules_assistant_refresh_models_failure", error.getMessage()),
            Resources.getString("Prefs.rules_assistant_refresh_models"),
            JOptionPane.ERROR_MESSAGE
          );
        }
      );
    }

    private String[] refreshedModelIds(String provider) throws IOException {
      if (MANUS_PROVIDER.equals(provider)) {
        return MANUS_MODEL_DEFAULTS;
      }
      if (XAI_PROVIDER.equals(provider)) {
        return XAI_MODEL_DEFAULTS;
      }

      final String apiKey = getApiKey(prefs);
      if (apiKey.isBlank()) {
        throw new IOException(Resources.getString("Prefs.rules_assistant_refresh_models_missing_key"));
      }

      final String response = HTTP.getJson(
        URI.create(baseUrlFor(provider).replaceAll("/+$", "") + "/models"), //NON-NLS
        Map.of("Authorization", "Bearer " + apiKey) //NON-NLS
      ).requireSuccess(provider);

      final String[] modelIds = modelIdsFromModelsResponse(response);
      if (modelIds.length == 0) {
        throw new IOException(Resources.getString("Prefs.rules_assistant_refresh_models_empty"));
      }
      return modelIds;
    }

    private String currentProvider() {
      final Object selectedProvider = providerConfigurer.getValue();
      if (selectedProvider instanceof String provider && !provider.isBlank()) {
        return provider;
      }
      return getProvider(prefs);
    }

    private void updateModelChoices(String provider, boolean keepCurrentValue) {
      updateModelChoices(provider, defaultModelsFor(provider), keepCurrentValue);
    }

    private void updateModelChoices(String provider, String[] modelIds, boolean keepCurrentValue) {
      final String currentValue = keepCurrentValue ? getValueString() : ""; //NON-NLS
      final Set<String> values = new LinkedHashSet<>();
      if (!currentValue.isBlank() && !AUTOMATIC_MODEL.equals(currentValue)) {
        values.add(currentValue);
      }
      values.addAll(Arrays.asList(modelIds));

      setValidValues(values.toArray(String[]::new));

      if (!currentValue.isBlank()) {
        setValue(currentValue);
      }
      else {
        setValue(AUTOMATIC_MODEL);
      }
    }

    private static String[] defaultModelsFor(String provider) {
      return switch (provider) {
      case XAI_PROVIDER -> XAI_MODEL_DEFAULTS;
      case MANUS_PROVIDER -> MANUS_MODEL_DEFAULTS;
      default -> OPENAI_MODEL_DEFAULTS;
      };
    }

    private static String baseUrlFor(String provider) {
      return switch (provider) {
      case XAI_PROVIDER -> XAI_BASE_URL;
      case MANUS_PROVIDER -> MANUS_BASE_URL;
      default -> OPENAI_BASE_URL;
      };
    }
  }

  static String[] modelIdsFromModelsResponse(String json) throws IOException {
    final Set<String> modelIds = new LinkedHashSet<>();
    final String idNeedle = "\"id\""; //NON-NLS
    int index = json.indexOf(idNeedle);
    while (index >= 0) {
      final String id = OpenAIRulesAssistantClient.jsonStringProperty(json.substring(index), "id"); //NON-NLS
      if (id != null && !id.isBlank()) {
        modelIds.add(id);
      }
      index = json.indexOf(idNeedle, index + idNeedle.length());
    }
    return modelIds.toArray(String[]::new);
  }
}
