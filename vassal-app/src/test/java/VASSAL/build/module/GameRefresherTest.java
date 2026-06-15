package VASSAL.build.module;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

public class GameRefresherTest {
  @Test
  public void selectedOptionLabelsReportsOptionsInUiOrder() {
    assertEquals(
      "refresh pieces, use counter names, fix Piece Id, refresh decks, add new decks",
      GameRefresher.selectedOptionLabels(Set.of(
        GameRefresher.ADD_NEW_DECKS,
        GameRefresher.FIX_GPID,
        GameRefresher.REFRESH_PIECES,
        GameRefresher.REFRESH_DECKS,
        GameRefresher.USE_NAME
      ))
    );
  }

  @Test
  public void selectedOptionsMessageFormatsChatterText() {
    assertEquals(
      "Refresh options: refresh pieces, trigger VassalPostRefreshGHK",
      GameRefresher.selectedOptionsMessage(Set.of(
        GameRefresher.REFRESH_PIECES,
        GameRefresher.USE_HOTKEY
      ))
    );
  }
}
