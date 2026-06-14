package VASSAL.build.module;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
  public void droppedSaveFileUrlIgnoresNonSaveFileText() {
    assertTrue(GameState.getDroppedSaveFileUrl("").isEmpty());
    assertTrue(GameState.getDroppedSaveFileUrl("   ").isEmpty());
    assertTrue(GameState.getDroppedSaveFileUrl("piece-drag").isEmpty());
    assertTrue(GameState.getDroppedSaveFileUrl("not a url").isEmpty());
    assertTrue(GameState.getDroppedSaveFileUrl("https://example.com/image.png").isEmpty());
    assertTrue(GameState.getDroppedSaveFileUrl("ftp://example.com/game.vsav").isEmpty());
  }

  @Test
  public void droppedSaveFileUrlAcceptsVassalSaveAndLogUrls() {
    final Optional<URL> save = GameState.getDroppedSaveFileUrl(" https://example.com/save.vsav ");
    final Optional<URL> log = GameState.getDroppedSaveFileUrl("https://example.com/game.vlog");
    final Optional<URL> localSave = GameState.getDroppedSaveFileUrl("file:///tmp/save.vsav");

    assertTrue(save.isPresent());
    assertEquals("https://example.com/save.vsav", save.get().toExternalForm());
    assertTrue(log.isPresent());
    assertEquals("https://example.com/game.vlog", log.get().toExternalForm());
    assertTrue(localSave.isPresent());
    assertEquals("file:/tmp/save.vsav", localSave.get().toExternalForm());
  }

  private static GamePiece piece(String id) {
    final GamePiece piece = mock(GamePiece.class);
    when(piece.getId()).thenReturn(id);
    when(piece.getState()).thenReturn("state-" + id);
    when(piece.getMap()).thenReturn(null);
    return piece;
  }
}
