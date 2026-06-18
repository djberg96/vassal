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

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.dice.DieServer;
import VASSAL.build.module.dice.QRandomDiceServer;
import VASSAL.build.module.dice.RandomOrgDiceServer;
import VASSAL.build.module.dice.RollSet;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.configure.BooleanConfigurer;
import VASSAL.configure.PasswordConfigurer;
import VASSAL.configure.StringArrayConfigurer;
import VASSAL.configure.StringConfigurer;
import VASSAL.configure.StringEnumConfigurer;
import VASSAL.i18n.Resources;
import VASSAL.preferences.Prefs;
import VASSAL.tools.FormattedString;

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
  private boolean useMultiRoll;
  private int defaultNDice = 2;
  private int defaultNSides = 6;

  private DieServer server;
  private String lastServerName = ""; //NON-NLS
  private MultiRoll myMultiRoll;
  final StringEnumConfigurer semail;

  public static final String USE_INTERNET_DICE = "useinternetdice"; //NON-NLS
  public static final String DICE_SERVER = "diceserver"; //NON-NLS
  public static final String SERVER_PW = "serverpw"; //NON-NLS
  public static final String USE_EMAIL = "useemail"; //NON-NLS
  public static final String PRIMARY_EMAIL = "primaryemail"; //NON-NLS
  public static final String SECONDARY_EMAIL = "secondaryemail"; //NON-NLS
  public static final String ADDRESS_BOOK = "addressbook"; //NON-NLS
  public static final String MULTI_ROLL = "multiroll"; //NON-NLS
  public static final String DIE_MANAGER = "Internet Die Roller"; //NON-NLS

  public static final String DESC = "description"; //NON-NLS
  public static final String DFLT_NSIDES = "dfltnsides"; //NON-NLS
  public static final String DFLT_NDICE = "dfltndice"; //NON-NLS
  public static final String RANDOM_ORG_DESCRIPTION = "RANDOM.ORG Signed API"; //NON-NLS
  public static final String Q_RANDOM_DESCRIPTION = "qrandom.io Quantum Dice (d6 only)"; //NON-NLS
  public static final String DEFAULT_DICE_SERVER = RANDOM_ORG_DESCRIPTION; //NON-NLS

  public DieManager() {

    servers = new LinkedHashMap<>();

    /*
     * Create the Internet Dice Servers we know about.
     */
    server = new RandomOrgDiceServer();
    registerServer(server);
    registerServer(new QRandomDiceServer());

    /*
     * The Dice Manager needs some preferences
     */

    final BooleanConfigurer useemail = new BooleanConfigurer(USE_EMAIL, "Email results?");
    final StringConfigurer pemail = new StringConfigurer(PRIMARY_EMAIL, "Primary Email");
    final StringArrayConfigurer abook = new StringArrayConfigurer(ADDRESS_BOOK, "Address Book");
    final BooleanConfigurer multiroll = new BooleanConfigurer(MULTI_ROLL, "Put multiple rolls into single email");

    GameModule.getGameModule().getPrefs().addOption(DIE_MANAGER, useemail);

    GameModule.getGameModule().getPrefs().addOption(DIE_MANAGER, abook);
    final String[] addressList = addressBookValues(GameModule.getGameModule().getPrefs().getValue(ADDRESS_BOOK));
    semail = new StringEnumConfigurer(SECONDARY_EMAIL, "Secondary Email", addressList);

    GameModule.getGameModule().getPrefs().addOption(DIE_MANAGER, pemail);
    GameModule.getGameModule().getPrefs().addOption(DIE_MANAGER, semail);
    GameModule.getGameModule().getPrefs().addOption(DIE_MANAGER, multiroll);

    setSemailValues();
    abook.addPropertyChangeListener(e -> setSemailValues());
  }

  private void registerServer(DieServer dieServer) {
    servers.put(dieServer.getName(), dieServer);
  }

  public void setSemailValues() {
    final String currentSemail = (String) GameModule.getGameModule().getPrefs().getValue(SECONDARY_EMAIL);
    final String[] addressBook = addressBookValues(GameModule.getGameModule().getPrefs().getValue(ADDRESS_BOOK));
    semail.setValidValues(addressBook);
    semail.setValue(currentSemail);
  }

  static String[] addressBookValues(Object value) {
    return value instanceof String[] ? (String[]) value : new String[0];
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

    final PasswordConfigurer serverKey = new PasswordConfigurer(
      SERVER_PW,
      Resources.getString("Prefs.internet_dice_api_key"),
      ""
    );

    final String tab = Resources.getString("Prefs.internet_dice_tab");
    prefs.addOption(tab, diceServer);
    prefs.addOption(tab, serverKey);
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

  public MultiRoll getMultiRoll(int nDice, int nSides) {
    getPrefs();
    return getMultiRollForSelectedServer(nDice, nSides);
  }

  private MultiRoll getMultiRollForSelectedServer(int nDice, int nSides) {
    final String serverName = server.getName();
    if (myMultiRoll == null || !serverName.equals(lastServerName)) {
      myMultiRoll = new MultiRoll(this, nDice, nSides);
    }
    lastServerName = serverName;

    return myMultiRoll;
  }

  public void roll(int nDice, int nSides, int plus, boolean reportTotal, String description, FormattedString format) {
    getPrefs();
    final MultiRoll mroll = getMultiRollForSelectedServer(nDice, nSides);
    rollConfigured(nDice, nSides, plus, reportTotal, description, format, mroll);
  }

  private void rollConfigured(
    int nDice,
    int nSides,
    int plus,
    boolean reportTotal,
    String description,
    FormattedString format,
    MultiRoll mroll
  ) {
    final RollSet rollSet;

    String desc = GameModule.getGameModule().getChatter().getInputField().getText();
    if (desc != null && desc.length() > 0) {
      mroll.setDescription(desc);
    }

    // Do we want full multi-roll capabilities? If required, pop-up the multi-roll
    // cofigurer to get the details
    if (useMultiRoll) {
      mroll.setVisible(true);

      if (mroll.wasCancelled()) {
        return;
      }
      rollSet = mroll.getRollSet();
      desc = rollSet.getDescription();
    }

    // Multi Roll preference not selected, so build a dummy MultiRoll object
    else {
      final DieRoll[] rolls = {new DieRoll(description, nDice, nSides, plus, reportTotal)};
      rollSet = new RollSet(description, rolls);
      desc = "";
    }

    final Command chatCommand = new Chatter.DisplayText(GameModule.getGameModule().getChatter(),
                                                  " - Roll sent to " + server.getDescription());

    if (desc == null || desc.length() == 0) {
      desc = GameModule.getGameModule().getChatter().getInputField().getText();
    }
    if (server.getUseEmail()) {
      if (desc == null || desc.length() == 0) {
        chatCommand.append(new Chatter.DisplayText(GameModule.getGameModule().getChatter(),
                                                   " - Emailing " + server.getSecondaryEmail() + " (no subject line)"));
        chatCommand.append(new Chatter.DisplayText(GameModule.getGameModule().getChatter(),
                                                   " - Leave text in the chat input area to provide a subject line"));
      }
      else {
        chatCommand.append(new Chatter.DisplayText(GameModule.getGameModule().getChatter(),
                                                   " - Emailing " + server.getSecondaryEmail() + " (Subject:  " + desc + ")"));
      }
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
    final Prefs modulePrefs = GameModule.getGameModule().getPrefs();
    final Prefs globalPrefs = Prefs.getGlobalPrefs();

    // Get the correct server
    final String serverName = stringPref(globalPrefs, DICE_SERVER, DEFAULT_DICE_SERVER);
    final DieServer selectedServer = getServerFromDescription(serverName);
    server = selectedServer == null ? servers.values().iterator().next() : selectedServer;

    // And tell it the prefs it will need
    server.setPasswd(stringPref(globalPrefs, SERVER_PW, ""));
    server.setUseEmail(booleanPref(modulePrefs, USE_EMAIL));
    server.setPrimaryEmail(stringPref(modulePrefs, PRIMARY_EMAIL, ""));
    server.setSecondaryEmail(stringPref(modulePrefs, SECONDARY_EMAIL, ""));

    useMultiRoll = booleanPref(modulePrefs, MULTI_ROLL);

  }

  private static String stringPref(Prefs prefs, String key, String defaultValue) {
    final Object value = prefs.getValue(key);
    if (value instanceof String s && !s.isBlank()) {
      return s;
    }
    return defaultValue;
  }

  private static boolean booleanPref(Prefs prefs, String key) {
    return Boolean.TRUE.equals(prefs.getValue(key));
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

  public void setSecondaryEmail(String email) {
    GameModule.getGameModule().getPrefs().setValue(SECONDARY_EMAIL, email);
    server.setSecondaryEmail(email);
  }
}
