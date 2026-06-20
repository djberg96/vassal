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

  public QRandomDiceServer() {
    name = "QRandom";
    description = DieManager.Q_RANDOM_DESCRIPTION;
    serverURL = "https://qrandom.io/api/random/dice";
    canDoSeparateDice = true;
  }

  @Override
  public int[] getnSideList() {
    return new int[]{6};
  }

  @Override
  public RollSet doIRoll(RollSet toss) throws IOException {
    for (final DieRoll roll : toss.getDieRolls()) {
      if (roll.getNumSides() != 6) {
        throw new IOException("qrandom.io supports only six-sided dice.");
      }

      final int[] results = requestDice(roll.getNumDice());
      applyResults(roll, results);
    }

    return toss;
  }

  protected int[] requestDice(int count) throws IOException {
    final String response = getJson(count);
    return parseDice(response);
  }

  protected String getJson(int count) throws IOException {
    return HTTP.getJson(URI.create(serverURL + "?n=" + count)) //NON-NLS
      .requireSuccess("qrandom.io"); //NON-NLS
  }

  static int[] parseDice(String response) throws IOException {
    JsonDieServerSupport.requireNoError(response);
    return JsonDieServerSupport.intArrayProperty(response, "dice");
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
