package VASSAL.build.module;

import VASSAL.build.GameModule;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.preferences.Prefs;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class MapTest {
  private static class TestMap extends Map {
    @Override
    public boolean shouldDockIntoMainWindow() {
      return false;
    }
  }

  private static class TestBoard extends Board {
    TestBoard(int width, int height, int column, int row) {
      setAttribute(WIDTH, width);
      setAttribute(HEIGHT, height);
      pos = new Point(column, row);
    }
  }

  private static Map newMap() {
    final GameModule module = mock(GameModule.class);
    final Prefs prefs = mock(Prefs.class);
    Mockito.when(module.getPrefs()).thenReturn(prefs);
    Mockito.when(prefs.getValue("scrollSpeed")).thenReturn(30);
    try (MockedStatic<GameModule> staticGm = Mockito.mockStatic(GameModule.class)) {
      staticGm.when(GameModule::getGameModule).thenReturn(module);
      return new TestMap();
    }
  }

  @Test
  void setBoardsLaysOutBoardsAndSetsOwningMap() {
    final Map map = newMap();
    map.edgeBuffer = new Dimension(10, 20);
    final Board left = new TestBoard(100, 50, 0, 0);
    final Board right = new TestBoard(80, 60, 1, 0);

    map.setBoards(List.of(left, right));

    assertSame(map, left.getMap());
    assertSame(map, right.getMap());
    assertEquals(new Dimension(200, 100), map.mapSize());
  }

  @Test
  void setBoardsReplacesPreviousBoardSet() {
    final Map map = newMap();
    map.setBoards(List.of(new TestBoard(100, 50, 0, 0)));

    map.setBoards(List.of(new TestBoard(40, 30, 0, 0)));

    assertEquals(1, map.getBoardCount());
    assertEquals(new Dimension(40, 30), map.mapSize());
  }

  @Test
  void getBoardsReturnsUnmodifiableCollection() {
    final Map map = newMap();
    map.setBoards(List.of(new TestBoard(100, 50, 0, 0)));

    final Collection<Board> boards = map.getBoards();

    assertThrows(UnsupportedOperationException.class, boards::clear);
  }
}
