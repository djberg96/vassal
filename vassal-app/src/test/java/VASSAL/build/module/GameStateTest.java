package VASSAL.build.module;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import VASSAL.command.AddPiece;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;

import java.net.URL;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class GameStateTest {

  @Test
  public void restorePiecesCommandOrdersUnmappedPiecesById() {
    final GameState state = new GameState();
    final GamePiece later = piece("b");
    final GamePiece earlier = piece("a");

    state.pieces.put(later.getId(), later);
    state.pieces.put(earlier.getId(), earlier);

    final Command command = state.getRestorePiecesCommand();
    final Command[] subCommands = command.getSubCommands();

    assertEquals(2, subCommands.length);
    assertSame(earlier, ((AddPiece) subCommands[0]).getTarget());
    assertSame(later, ((AddPiece) subCommands[1]).getTarget());
  }

  @Test
  public void droppedTextUrlIgnoresBlankRelativeAndInvalidText() {
    assertTrue(GameState.getDroppedTextUrl("").isEmpty());
    assertTrue(GameState.getDroppedTextUrl("   ").isEmpty());
    assertTrue(GameState.getDroppedTextUrl("piece-drag").isEmpty());
    assertTrue(GameState.getDroppedTextUrl("not a url").isEmpty());
  }

  @Test
  public void droppedTextUrlAcceptsAbsoluteUrls() {
    final Optional<URL> url = GameState.getDroppedTextUrl(" https://example.com/save.vsav ");

    assertTrue(url.isPresent());
    assertEquals("https://example.com/save.vsav", url.get().toExternalForm());
  }

  private static GamePiece piece(String id) {
    final GamePiece piece = mock(GamePiece.class);
    when(piece.getId()).thenReturn(id);
    when(piece.getState()).thenReturn("state-" + id);
    when(piece.getMap()).thenReturn(null);
    return piece;
  }
}
