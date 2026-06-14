package VASSAL.build;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.function.Function;

import VASSAL.command.Command;
import org.junit.jupiter.api.Test;

public class GameModuleTest {
  private static final char COMMAND_SEPARATOR = KeyEvent.VK_ESCAPE;

  private static class EncodedCommand extends Command {
    private final String code;

    private EncodedCommand(String code) {
      this.code = code;
    }

    @Override
    protected void executeCommand() {
    }

    @Override
    protected Command myUndoCommand() {
      return null;
    }
  }

  private static final Function<Command, String> ENCODER = c -> ((EncodedCommand) c).code;

  private static String writeEncoded(Command c) throws IOException {
    final StringWriter out = new StringWriter();
    GameModule.writeEncoded(c, ENCODER, out);
    return out.toString();
  }

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

  @Test
  public void writeEncodedWritesAtomicCommand() throws IOException {
    final Command command = new EncodedCommand("ROOT");

    assertEquals("ROOT", writeEncoded(command));
  }

  @Test
  public void writeEncodedSeparatesCompoundCommands() throws IOException {
    final Command command = new EncodedCommand("ROOT")
      .append(new EncodedCommand("A"))
      .append(new EncodedCommand("B"));

    assertEquals("ROOT" + COMMAND_SEPARATOR + "A" + COMMAND_SEPARATOR + "B", writeEncoded(command));
  }

  @Test
  public void writeEncodedEscapesCommandSeparatorsInElements() throws IOException {
    final Command command = new EncodedCommand("ROOT")
      .append(new EncodedCommand("A" + COMMAND_SEPARATOR + "B"));

    assertEquals("ROOT" + COMMAND_SEPARATOR + "A\\" + COMMAND_SEPARATOR + "B", writeEncoded(command));
  }

  @Test
  public void writeEncodedEscapesNestedCompoundCommands() throws IOException {
    final Command child = new EncodedCommand("CHILD")
      .append(new EncodedCommand("GRANDCHILD"));
    final Command command = new EncodedCommand("ROOT").append(child);

    assertEquals(
      "ROOT" + COMMAND_SEPARATOR + "CHILD\\" + COMMAND_SEPARATOR + "GRANDCHILD",
      writeEncoded(command)
    );
  }
}
