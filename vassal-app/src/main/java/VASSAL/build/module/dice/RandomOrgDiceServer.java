package VASSAL.build.module.dice;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

import VASSAL.build.module.DieManager;
import VASSAL.build.module.DieRoll;
import VASSAL.tools.http.HttpClientService;

public class RandomOrgDiceServer extends DieServer {
  private static final HttpClientService HTTP =
    HttpClientService.createDefault(Duration.ofSeconds(30));

  public RandomOrgDiceServer() {
    name = "RandomOrg";
    description = DieManager.RANDOM_ORG_DESCRIPTION;
    serverURL = "https://api.random.org/json-rpc/4/invoke";
    passwdRequired = true;
    canDoSeparateDice = true;
  }

  @Override
  public RollSet doIRoll(RollSet toss) throws IOException {
    if (password == null || password.isBlank()) {
      throw new IOException("RANDOM.ORG API key is required. Set it in Preferences > Internet Dice.");
    }

    int requestId = 1;
    for (final DieRoll roll : toss.getDieRolls()) {
      final int[] results = requestSignedIntegers(requestId++, roll.getNumDice(), 1, roll.getNumSides());
      applyResults(roll, results);
    }

    return toss;
  }

  protected int[] requestSignedIntegers(int requestId, int count, int min, int max) throws IOException {
    final String response = postJson(signedIntegersRequest(password, requestId, count, min, max));
    return parseSignedIntegers(response);
  }

  protected String postJson(String requestBody) throws IOException {
    return HTTP.postJson(URI.create(serverURL), requestBody)
      .requireSuccess("RANDOM.ORG"); //NON-NLS
  }

  static String signedIntegersRequest(String apiKey, int id, int count, int min, int max) {
    return "{"
      + "\"jsonrpc\":\"2.0\","
      + "\"method\":\"generateSignedIntegers\","
      + "\"params\":{"
      + "\"apiKey\":" + JsonDieServerSupport.jsonString(apiKey) + ','
      + "\"n\":" + count + ','
      + "\"min\":" + min + ','
      + "\"max\":" + max + ','
      + "\"replacement\":true,"
      + "\"base\":10"
      + "},"
      + "\"id\":" + id
      + '}';
  }

  static int[] parseSignedIntegers(String response) throws IOException {
    JsonDieServerSupport.requireNoError(response);
    return JsonDieServerSupport.intArrayProperty(response, "data");
  }

  private static void applyResults(DieRoll roll, int[] results) throws IOException {
    if (results.length != roll.getNumDice()) {
      throw new IOException("RANDOM.ORG returned " + results.length + " dice for a " + roll.getNumDice() + "-die roll.");
    }

    for (int i = 0; i < results.length; i++) {
      roll.setResult(i, results[i]);
    }
  }

}
