package VASSAL.build.module.dice;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import VASSAL.build.module.DieRoll;

public class QRandomDiceServer extends DieServer {
  private static final int HTTP_TIMEOUT_MS = 30_000;

  public QRandomDiceServer() {
    name = "QRandom";
    description = "qrandom.io Quantum Dice (d6 only)";
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
    final HttpURLConnection connection =
      (HttpURLConnection) URI.create(serverURL + "?n=" + count).toURL().openConnection();
    connection.setConnectTimeout(HTTP_TIMEOUT_MS);
    connection.setReadTimeout(HTTP_TIMEOUT_MS);
    connection.setRequestProperty("Accept", "application/json");

    return readConnectionResponse(connection);
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

  private static String readConnectionResponse(HttpURLConnection connection) throws IOException {
    final int status = connection.getResponseCode();
    final InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
    final String response = stream == null ? "" : readUtf8(stream);
    if (status >= 400) {
      throw new IOException("qrandom.io returned HTTP " + status + ": " + response);
    }
    return response;
  }
}
