package VASSAL.build.module.dice;

import VASSAL.build.GameModule;
import VASSAL.build.module.DiceButton;
import VASSAL.build.module.DieRoll;
import VASSAL.build.module.InternetDiceButton;
import VASSAL.script.expression.Auditable;
import VASSAL.tools.ErrorDialog;
import VASSAL.tools.FormattedString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base DieServer Class
 * Does the common async/reporting work. Individual die servers fetch their
 * results by implementing {@link #doIRoll(RollSet)}.
 */
public abstract class DieServer implements Auditable {
  private static final Logger logger = LoggerFactory.getLogger(DieServer.class);

  protected java.util.Random ran;
  protected String name;
  protected String description;
  protected boolean emailOnly;
  protected int maxRolls;
  protected int maxEmails;
  protected String serverURL;
  protected boolean passwdRequired = false;
  protected String password = "";  //NON-NLS
  protected boolean useEmail;
  protected String primaryEmail;
  protected String secondaryEmail;
  protected boolean canDoSeparateDice = false;

  /*
   * Internet Die Servers should always implement roll by calling back to
   * {@link #doInternetRoll}
   */
  public void roll(RollSet mr, FormattedString format) {
    doInternetRoll(mr, format);
  }

  public DieServer() {
    final GameModule module = GameModule.getGameModule();
    ran = module == null ? new Random() : module.getRNG();
  }

  /*
   * Some Internet servers can only roll specific numbers of dice or
   * dice with specific sides. These are the default settings.
   */
  public int[] getnDiceList() {
    return new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
  }

  public int[] getnSideList() {
    return new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 20, 30, 50, 100, 1000};
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public boolean isPasswdRequired() {
    return passwdRequired;
  }

  public String getPasswd() {
    return password;
  }

  public void setPasswd(String s) {
    password = s;
  }

  public void setPrimaryEmail(String e) {
    primaryEmail = e;
  }

  public String getPrimaryEmail() {
    return primaryEmail;
  }

  public void setSecondaryEmail(String e) {
    secondaryEmail = e;
  }

  public String getSecondaryEmail() {
    return secondaryEmail;
  }

  public void setUseEmail(boolean use) {
    useEmail = use;
  }

  public boolean getUseEmail() {
    return useEmail;
  }

  public int getMaxEmails() {
    return maxEmails;
  }

  /**
   * The text reported before the results of the roll
   */
  protected String getReportPrefix(String d) {
    return " *** " + d + " = "; //NON-NLS
  }

  /*
   * Internet Servers will call this routine to do their dirty work.
   */
  public void doInternetRoll(final RollSet mroll, final FormattedString format) {
    new SwingWorker<RollSet, Void>() {
      @Override
      public RollSet doInBackground() throws Exception {
        return rollInBackground(mroll);
      }

      @Override
      protected void done() {
        try {
          reportResult(get(), format);
        }
        catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          ErrorDialog.bug(e);
        }
        catch (ExecutionException e) {
          logger.error("", e);
          GameModule.getGameModule().getChatter().send(internetRollFailureMessage(mroll, e.getCause()));
        }
      }
    }.execute();
  }

  protected RollSet rollInBackground(RollSet rollSet) throws IOException {
    return doIRoll(rollSet);
  }

  static String internetRollFailureMessage(RollSet rollSet, Throwable cause) {
    final StringBuilder message = new StringBuilder("- Internet dice roll attempt "); //NON-NLS
    message.append(rollSet.getDescription()).append(" failed"); //NON-NLS

    if (cause != null && cause.getMessage() != null && !cause.getMessage().isBlank()) {
      message.append(": ").append(cause.getMessage()); //NON-NLS
    }

    return message.append('.').toString();
  }

  /**
   * Use the configured FormattedString to format the result of a roll
   * @param description Roll Description
   * @param result Roll Result
   * @param format Report Format
   * @return Formatted roll result
   */
  protected String formatResult(String description, String result, FormattedString format) {
    format.setProperty(DiceButton.RESULT, result);
    format.setProperty(InternetDiceButton.DETAILS, description);
    final String text = format.getText(this, "Editor.report_format");
    return text.startsWith("*") ? "*" + text : "* " + text;
  }


  public void reportResult(RollSet mroll, FormattedString format) {
    final DieRoll[] rolls = mroll.getDieRolls();
    for (final DieRoll roll : rolls) {
      final int nDice = roll.getNumDice();
      final boolean reportTotal = roll.isReportTotal();

      final StringBuilder val = new StringBuilder();
      int total = 0;

      for (int j = 0; j < nDice; j++) {
        final int result = roll.getResult(j);
        if (reportTotal) {
          total += result;
        }
        else {
          val.append(result);
          if (j < nDice - 1)
            val.append(',');
        }
      }

      if (reportTotal)
        val.append(total);

      GameModule.getGameModule().getChatter().send(formatResult(roll.getDescription(), val.toString(), format));
    }
  }

  public abstract RollSet doIRoll(RollSet toss) throws IOException;

  protected static String readUtf8(InputStream in) throws IOException {
    try (InputStream input = in;
         ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      input.transferTo(out);
      return out.toString(StandardCharsets.UTF_8);
    }
  }

  /*
   * Extract the portion of the email address withing the  angle brackets.
   * Allows Email addresses like 'Joe Blow <j.blow@somewhere.com>'
   */
  public String extractEmail(String email) {
    final int start = email.indexOf('<');
    final int end = email.indexOf('>');
    if (start >= 0 && end >= 0 && end > start) {
      return email.substring(start + 1, end);
    }
    else {
      return email;
    }
  }
}
