package VASSAL.build;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class GameModuleTest {
  @Test
  public void recentGamesFromPrefsValueTreatsNullAsEmpty() {
    assertEquals(List.of(), GameModule.recentGamesFromPrefsValue(null));
  }

  @Test
  public void recentGamesFromPrefsValueReversesStoredOrder() {
    assertEquals(
      List.of("third.vsav", "second.vsav", "first.vsav"),
      GameModule.recentGamesFromPrefsValue(new String[] {"first.vsav", "second.vsav", "third.vsav"})
    );
  }

  @Test
  public void recentGamesFromPrefsValueSkipsBlankEntries() {
    assertEquals(
      List.of("second.vsav", "first.vsav"),
      GameModule.recentGamesFromPrefsValue(new String[] {"first.vsav", "", null, "second.vsav"})
    );
  }
}
