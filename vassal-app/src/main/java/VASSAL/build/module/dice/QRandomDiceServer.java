package VASSAL.build.module.dice;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

import VASSAL.build.module.DieManager;
import VASSAL.build.module.DieRoll;
import VASSAL.tools.http.HttpClientService;

public class QRandomDiceServer extends DieServer {
  private static final HttpClientService HTTP =
    HttpClientService.createDefault(Duration.ofSeconds(30));
  private static final String NUMBER_URL = "https://qrandom.io/api/random/ints"; //NON-NLS

  public QRandomDiceServer() {
    name = "QRandom";
    description = DieManager.Q_RANDOM_DESCRIPTION;
    serverURL = NUMBER_URL;
    canDoSeparateDice = true;
  }

  @Override
  public RollSet doIRoll(RollSet toss) throws IOException {
    for (final DieRoll roll : toss.getDieRolls()) {
      final int[] results = requestIntegers(roll.getNumDice(), 1, roll.getNumSides());
      applyResults(roll, results);
    }

    return toss;
  }

  protected int[] requestIntegers(int count, int min, int max) throws IOException {
    final String response = getIntegersJson(count, min, max);
    return parseIntegers(response);
  }

  protected String getIntegersJson(int count, int min, int max) throws IOException {
    return HTTP.getJson(URI.create(serverURL + "?min=" + min + "&max=" + max + "&n=" + count)) //NON-NLS
      .requireSuccess("qrandom.io"); //NON-NLS
  }

  static int[] parseIntegers(String response) throws IOException {
    JsonDieServerSupport.requireNoError(response);
    return JsonDieServerSupport.intArrayProperty(response, "numbers");
  }

  private static void applyResults(DieRoll roll, int[] results) throws IOException {
    if (results.length != roll.getNumDice()) {
      throw new IOException("qrandom.io returned " + results.length + " dice for a " + roll.getNumDice() + "-die roll.");
    }

    for (int i = 0; i < results.length; i++) {
      roll.setResult(i, results[i]);
    }
  }

}
