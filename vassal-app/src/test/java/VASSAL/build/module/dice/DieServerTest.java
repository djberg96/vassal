package VASSAL.build.module.dice;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import VASSAL.build.module.DieRoll;
import org.junit.jupiter.api.Test;

class DieServerTest {
  private static class TestDieServer extends DieServer {
    private boolean rolled;

    @Override
    public RollSet doIRoll(RollSet toss) {
      rolled = true;
      toss.getDieRolls()[0].setResult(0, 4);
      return toss;
    }
  }

  private static class TestQRandomDiceServer extends QRandomDiceServer {
    @Override
    protected int[] requestIntegers(int count, int min, int max) {
      final int[] results = new int[count];
      for (int i = 0; i < count; i++) {
        results[i] = max - i;
      }
      return results;
    }
  }

  private static class TestRandomOrgDiceServer extends RandomOrgDiceServer {
    @Override
    protected int[] requestSignedIntegers(int requestId, int count, int min, int max) {
      final int[] results = new int[count];
      for (int i = 0; i < count; i++) {
        results[i] = max - i;
      }
      return results;
    }
  }

  @Test
  void rollInBackgroundReturnsCompletedRollSet() throws IOException {
    final TestDieServer server = new TestDieServer();
    final RollSet rollSet = new RollSet("Attack", new DieRoll[] {
      new DieRoll("red", 1, 6)
    });

    final RollSet result = server.rollInBackground(rollSet);

    assertSame(rollSet, result);
    assertTrue(server.rolled);
    assertEquals(4, result.getDieRolls()[0].getResult(0));
  }

  @Test
  void internetRollFailureMessageIncludesDescriptionAndCause() {
    final RollSet rollSet = new RollSet("Attack", new DieRoll[] {
      new DieRoll("red", 1, 6)
    });

    assertEquals(
      "- Internet dice roll attempt Attack failed: network unavailable.",
      DieServer.internetRollFailureMessage(rollSet, new IOException("network unavailable"))
    );
  }

  @Test
  void internetRollFailureMessageOmitsBlankCause() {
    final RollSet rollSet = new RollSet("Attack", new DieRoll[] {
      new DieRoll("red", 1, 6)
    });

    assertEquals(
      "- Internet dice roll attempt Attack failed.",
      DieServer.internetRollFailureMessage(rollSet, new IOException(" "))
    );
  }

  @Test
  void qrandomParsesIntegerArrayResponse() throws IOException {
    assertArrayEquals(
      new int[]{8, 3, 6, 1},
      QRandomDiceServer.parseIntegers("""
        {
          "message": "Request: From: 1, To: 8, Amount: 4",
          "numbers": [8, 3, 6, 1],
          "signature": "abc",
          "resultType": "randomIntArray"
        }
        """)
    );
  }

  @Test
  void qrandomAppliesIntegerResults() throws IOException {
    final QRandomDiceServer server = new TestQRandomDiceServer();
    final RollSet rollSet = new RollSet("Attack", new DieRoll[] {
      new DieRoll("2d8", 2, 8)
    });

    server.doIRoll(rollSet);

    assertEquals(8, rollSet.getDieRolls()[0].getResult(0));
    assertEquals(7, rollSet.getDieRolls()[0].getResult(1));
  }

  @Test
  void randomOrgBuildsSignedIntegerRequest() {
    assertEquals(
      "{\"jsonrpc\":\"2.0\",\"method\":\"generateSignedIntegers\","
        + "\"params\":{\"apiKey\":\"key\\nvalue\",\"n\":2,\"min\":1,\"max\":6,\"replacement\":true,\"base\":10},"
        + "\"id\":7}",
      RandomOrgDiceServer.signedIntegersRequest("key\nvalue", 7, 2, 1, 6)
    );
  }

  @Test
  void randomOrgParsesSignedIntegerResponse() throws IOException {
    assertArrayEquals(
      new int[]{6, 1, 4},
      RandomOrgDiceServer.parseSignedIntegers("""
        {
          "jsonrpc": "2.0",
          "result": {
            "random": {
              "method": "generateSignedIntegers",
              "data": [6, 1, 4]
            },
            "signature": "signed"
          },
          "id": 1
        }
        """)
    );
  }

  @Test
  void randomOrgReportsServiceErrors() {
    final IOException e = assertThrows(
      IOException.class,
      () -> RandomOrgDiceServer.parseSignedIntegers("""
        {
          "jsonrpc": "2.0",
          "error": {
            "code": 200,
            "message": "API key is invalid"
          },
          "id": 1
        }
        """)
    );

    assertEquals("Dice server returned an error: API key is invalid", e.getMessage());
  }

  @Test
  void randomOrgRequiresApiKey() {
    final RandomOrgDiceServer server = new RandomOrgDiceServer();
    final RollSet rollSet = new RollSet("Attack", new DieRoll[] {
      new DieRoll("2d6", 2, 6)
    });

    final IOException e = assertThrows(IOException.class, () -> server.doIRoll(rollSet));

    assertEquals(
      "RANDOM.ORG API key is required. Set it in Preferences > Internet Dice.",
      e.getMessage()
    );
  }

  @Test
  void randomOrgAppliesSignedResults() throws IOException {
    final RandomOrgDiceServer server = new TestRandomOrgDiceServer();
    server.setPasswd("test-key");
    final RollSet rollSet = new RollSet("Attack", new DieRoll[] {
      new DieRoll("2d6", 2, 6)
    });

    server.doIRoll(rollSet);

    assertEquals(6, rollSet.getDieRolls()[0].getResult(0));
    assertEquals(5, rollSet.getDieRolls()[0].getResult(1));
  }
}
