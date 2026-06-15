package VASSAL.build.module.dice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Vector;

import VASSAL.build.module.DieRoll;
import VASSAL.tools.FormattedString;
import org.junit.jupiter.api.Test;

class DieServerTest {
  private static class TestDieServer extends DieServer {
    private boolean rolled;

    @Override
    public String[] buildInternetRollString(RollSet mr) {
      return new String[0];
    }

    @Override
    public void parseInternetRollString(RollSet rollSet, Vector<String> results) {
    }

    @Override
    public void roll(RollSet mr, FormattedString format) {
    }

    @Override
    public RollSet doIRoll(RollSet toss) {
      rolled = true;
      toss.getDieRolls()[0].setResult(0, 4);
      return toss;
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
}
