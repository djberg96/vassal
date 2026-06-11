package VASSAL.build.module.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import VASSAL.build.GameModule;
import VASSAL.build.module.GlobalKeyCommand;
import VASSAL.counters.GlobalCommandTarget;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MassKeyCommandTest {
  @BeforeAll
  static void initModule() throws Exception {
    if (GameModule.getGameModule() == null) {
      GameModule.init(mock(GameModule.class));
    }
  }

  @Test
  public void commandsInitializeTargetsWithTheirOwnType() {
    assertEquals(GlobalCommandTarget.GKCtype.MAP, new MassKeyCommand().getTarget().getGKCtype());
    assertEquals(GlobalCommandTarget.GKCtype.MODULE, new GlobalKeyCommand().getTarget().getGKCtype());
    assertEquals(GlobalCommandTarget.GKCtype.DECK, new DeckGlobalKeyCommand().getTarget().getGKCtype());
  }

  @Test
  public void copiedCommandsCoerceTargetsToTheirOwnType() {
    final GlobalCommandTarget sourceTarget = new GlobalCommandTarget(GlobalCommandTarget.GKCtype.MAP);
    sourceTarget.setFastMatchLocation(true);
    sourceTarget.setFastMatchProperty(true);

    final MassKeyCommand source = new MassKeyCommand();
    source.setAttribute(MassKeyCommand.TARGET, sourceTarget);

    final GlobalKeyCommand moduleCommand = new GlobalKeyCommand(source);
    assertEquals(GlobalCommandTarget.GKCtype.MODULE, moduleCommand.getTarget().getGKCtype());
    assertTrue(moduleCommand.getTarget().isFastMatchLocation());
    assertTrue(moduleCommand.getTarget().isFastMatchProperty());

    final DeckGlobalKeyCommand deckCommand = new DeckGlobalKeyCommand(source);
    assertEquals(GlobalCommandTarget.GKCtype.DECK, deckCommand.getTarget().getGKCtype());
    assertFalse(deckCommand.getTarget().isFastMatchLocation());
    assertFalse(deckCommand.getTarget().isFastMatchProperty());
  }
}
