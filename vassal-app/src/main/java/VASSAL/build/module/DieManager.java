/*
 *
 * Copyright (c) 2000-2003 by Brent Easton
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
package VASSAL.build.module;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.awt.Component;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.concurrent.Future;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.dice.DieServer;
import VASSAL.build.module.dice.QRandomDiceServer;
import VASSAL.build.module.dice.RandomOrgDiceServer;
import VASSAL.build.module.dice.RollSet;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.configure.PasswordConfigurer;
import VASSAL.configure.StringEnumConfigurer;
import VASSAL.i18n.Resources;
import VASSAL.preferences.Prefs;
import VASSAL.tools.FormattedString;
import VASSAL.tools.concurrent.BackgroundTasks;

/**
 * @author Brent Easton
 *
 * Internet Die Roller Manager. Includes all the smarts to interface to web-based
 * Die Servers
 */

public final class DieManager extends AbstractConfigurable {
  private final Map<String, DieServer> servers;
  private final List<InternetDiceButton> dieButtons = new ArrayList<>();
  private String desc = "Die Manager"; //NON-NLS
  private int defaultNDice = 2;
  private int defaultNSides = 6;

  private DieServer server;

  public static final String USE_INTERNET_DICE = "useinternetdice"; //NON-NLS
  public static final String DICE_SERVER = "diceserver"; //NON-NLS
  public static final String SERVER_PW = "serverpw"; //NON-NLS

  public static final String DESC = "description"; //NON-NLS
  public static final String DFLT_NSIDES = "dfltnsides"; //NON-NLS
  public static final String DFLT_NDICE = "dfltndice"; //NON-NLS
  public static final String RANDOM_ORG_DESCRIPTION = "random.org"; //NON-NLS
  public static final String Q_RANDOM_DESCRIPTION = "qrandom.io"; //NON-NLS
  public static final String DEFAULT_DICE_SERVER = RANDOM_ORG_DESCRIPTION; //NON-NLS

  public DieManager() {

    servers = new LinkedHashMap<>();

    /*
     * Create the Internet Dice Servers we know about.
     */
    server = new RandomOrgDiceServer();
    registerServer(server);
    registerServer(new QRandomDiceServer());
  }

  private void registerServer(DieServer dieServer) {
    servers.put(dieServer.getName(), dieServer);
  }

  public static String[] getAvailableServerDescriptions() {
    return new String[] { RANDOM_ORG_DESCRIPTION, Q_RANDOM_DESCRIPTION };
  }

  public static void addGlobalPreferences(Prefs prefs) {
    final StringEnumConfigurer diceServer = new StringEnumConfigurer(
      DICE_SERVER,
      Resources.getString("Prefs.internet_dice_server"),
      getAvailableServerDescriptions()
    );
    diceServer.setValue(DEFAULT_DICE_SERVER);

    final TrimmingPasswordConfigurer serverKey = new TrimmingPasswordConfigurer(
      prefs,
      SERVER_PW,
      Resources.getString("Prefs.internet_dice_api_key"),
      ""
    );

    final String tab = Resources.getString("Prefs.internet_dice_tab");
    prefs.addOption(tab, diceServer);
    prefs.addOption(tab, serverKey);
  }

  static void verifyInternetDice(Prefs prefs) throws IOException {
    final DieServer testServer = createServerFromDescription(stringPref(prefs, DICE_SERVER, DEFAULT_DICE_SERVER));
    testServer.setPasswd(stringPref(prefs, SERVER_PW, ""));

    if (testServer.isPasswdRequired() && testServer.getPasswd().isBlank()) {
      throw new IOException(Resources.getString("Prefs.internet_dice_verify_missing_key"));
    }

    final RollSet rollSet = new RollSet("verification", new DieRoll[] { new DieRoll("d6", 1, 6) });
    testServer.doIRoll(rollSet);
  }

  private static DieServer createServerFromDescription(String description) {
    if (Q_RANDOM_DESCRIPTION.equals(description)) {
      return new QRandomDiceServer();
    }
    return new RandomOrgDiceServer();
  }

  // Return names of all known Dice Servers
  public String[] getNames() {
    return servers.keySet().toArray(new String[0]);
  }

  // Return descriptions of all known dice servers
  public String[] getDescriptions() {
    final String[] s = new String[servers.size()];
    int i = 0;
    for (final DieServer d : servers.values()) {
      s[i++] = d.getDescription();
    }
    return s;
  }

  // Return server matching Name
  public DieServer getServerForName(String name) {
    return servers.get(name);
  }

  // Return server matching Description
  public DieServer getServerFromDescription(String de) {
    for (final DieServer d : servers.values()) {
      if (d.getDescription().equals(de)) {
        return d;
      }
    }
    return null;
  }

  public DieServer getServer() {
    getPrefs();
    return server;
  }

  public String getServerDescription() {
    return getServer().getDescription();
  }

  public String getServerName() {
    return getServer().getName();
  }

  public int getDfltNDice() {
    return defaultNDice;
  }

  public int getDfltNSides() {
    return defaultNSides;
  }

  public void roll(int nDice, int nSides, int plus, boolean reportTotal, String description, FormattedString format) {
    getPrefs();
    rollConfigured(nDice, nSides, plus, reportTotal, description, format);
  }

  private void rollConfigured(
    int nDice,
    int nSides,
    int plus,
    boolean reportTotal,
    String description,
    FormattedString format
  ) {
    String desc = GameModule.getGameModule().getChatter().getInputField().getText();
    final DieRoll[] rolls = {new DieRoll(description, nDice, nSides, plus, reportTotal)};
    final RollSet rollSet = new RollSet(description, rolls);

    final Command chatCommand = new Chatter.DisplayText(GameModule.getGameModule().getChatter(),
                                                  " - Roll sent to " + server.getDescription());

    if (desc == null || desc.length() == 0) {
      desc = GameModule.getGameModule().getChatter().getInputField().getText();
    }
    chatCommand.execute();
    GameModule.getGameModule().sendAndLog(chatCommand);

    GameModule.getGameModule().getChatter().getInputField().setText("");
    rollSet.setDescription(desc);

    server.roll(rollSet, format);
  }

  /*
   * Retrieve the Dice Manager preferences and update the current Server
   * Preferences may change at ANY time!
   */
  private void getPrefs() {
    final Prefs globalPrefs = Prefs.getGlobalPrefs();

    // Get the correct server
    final String serverName = stringPref(globalPrefs, DICE_SERVER, DEFAULT_DICE_SERVER);
    final DieServer selectedServer = getServerFromDescription(serverName);
    server = selectedServer == null ? servers.values().iterator().next() : selectedServer;

    // And tell it the prefs it will need
    server.setPasswd(stringPref(globalPrefs, SERVER_PW, ""));
  }

  private static String stringPref(Prefs prefs, String key, String defaultValue) {
    final Object value = prefs.getValue(key);
    if (value instanceof String s && !s.isBlank()) {
      return s.strip();
    }
    return defaultValue;
  }

  public boolean canUseInternetDice() {
    getPrefs();
    return !server.isPasswdRequired() || !server.getPasswd().isBlank();
  }

  /*
      public void addDie(SpecialDie d) {
          specialDice.add(d);
      }

      public void removeDie(SpecialDie d) {
          specialDice.remove(d);
      }
  */

  public void addDieButton(InternetDiceButton d) {
    dieButtons.add(d);
  }

  public void removeDieButton(InternetDiceButton d) {
    dieButtons.remove(d);
  }

  @Override
  public String[] getAttributeDescriptions() {
    return new String[]{
        Resources.getString("Editor.description_label"), //$NON-NLS-1$
        Resources.getString("Editor.DieManager.ndice"), //$NON-NLS-1$
        Resources.getString("Editor.DieManager.nsides") //$NON-NLS-1$
    };
  }

  @Override
  public Class<?>[] getAttributeTypes() {
    return new Class<?>[]{
      String.class,
      Integer.class,
      Integer.class
    };
  }

  @Override
  public String[] getAttributeNames() {
    return new String[]{
      DESC,
      DFLT_NDICE,
      DFLT_NSIDES
    };
  }

  @Override
  public void setAttribute(String key, Object value) {
    if (DESC.equals(key)) {
      desc = (String) value;
    }
    else if (DFLT_NDICE.equals(key)) {
      if (value instanceof String) {
        value = Integer.valueOf((String) value);
      }
      defaultNDice = (Integer) value;
    }
    else if (DFLT_NSIDES.equals(key)) {
      if (value instanceof String) {
        value = Integer.valueOf((String) value);
      }
      defaultNSides = (Integer) value;
    }
  }

  @Override
  public String getAttributeValueString(String key) {
    if (DESC.equals(key)) {
      return desc;
    }
    else if (DFLT_NDICE.equals(key)) {
      return Integer.toString(defaultNDice);
    }
    else if (DFLT_NSIDES.equals(key)) {
      return Integer.toString(defaultNSides);
    }
    else
      return null;
  }

  @Override
  public void removeFrom(Buildable parent) {
  }

  @Override
  public HelpFile getHelpFile() {
    return null;
  }

  @Override
  public Class<?>[] getAllowableConfigureComponents() {
    return new Class<?>[]{InternetDiceButton.class};
  }

  @Override
  public void addTo(Buildable parent) {
  }

  public static String getConfigureTypeName() {
    return Resources.getString("Editor.DieManager.component_type"); //$NON-NLS-1$
  }

  static final class TrimmingPasswordConfigurer extends PasswordConfigurer {
    private static final int API_KEY_COLUMNS = 32;

    private final Prefs prefs;
    private Component controls;
    private JButton verifyButton;
    private JButton showButton;
    private char maskedEchoChar;
    private Future<?> verifyTask;

    TrimmingPasswordConfigurer(Prefs prefs, String key, String name, String val) {
      super(key, name, strip(val));
      this.prefs = prefs;
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

      verifyButton = new JButton(Resources.getString("Prefs.internet_dice_verify_button"));
      verifyButton.addActionListener(e -> verify());

      showButton = new JButton(Resources.getString("Prefs.internet_dice_show_key"));
      showButton.addActionListener(e -> toggleKeyVisibility(passwordField));

      final JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
      inputRow.setOpaque(false);
      inputRow.add(passwordControl);
      inputRow.add(verifyButton);
      inputRow.add(showButton);
      p.add(inputRow, "growx");
      return controls;
    }

    private void toggleKeyVisibility(JPasswordField passwordField) {
      final boolean hidden = passwordField.getEchoChar() != 0;
      passwordField.setEchoChar(hidden ? (char) 0 : maskedEchoChar);
      showButton.setText(Resources.getString(hidden ? "Prefs.internet_dice_hide_key" : "Prefs.internet_dice_show_key"));
    }

    private void verify() {
      if (verifyTask != null && !verifyTask.isDone()) {
        return;
      }

      verifyButton.setEnabled(false);
      verifyTask = BackgroundTasks.submitWithCallbacksOnEdt(
        () -> {
          verifyInternetDice(prefs);
          return null;
        },
        ignored -> {
          verifyButton.setEnabled(true);
          JOptionPane.showMessageDialog(
            verifyButton,
            Resources.getString("Prefs.internet_dice_verify_success"),
            Resources.getString("Prefs.internet_dice_verify"),
            JOptionPane.INFORMATION_MESSAGE
          );
        },
        error -> {
          verifyButton.setEnabled(true);
          JOptionPane.showMessageDialog(
            verifyButton,
            Resources.getString("Prefs.internet_dice_verify_failure", error.getMessage()),
            Resources.getString("Prefs.internet_dice_verify"),
            JOptionPane.ERROR_MESSAGE
          );
        }
      );
    }

    private static String strip(String value) {
      return value == null ? "" : value.strip();
    }
  }
}
