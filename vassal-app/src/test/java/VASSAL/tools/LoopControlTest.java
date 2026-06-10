package VASSAL.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LoopControlTest {

  @Test
  public void loopTypesAreImmutable() {
    assertThrows(UnsupportedOperationException.class, () -> LoopControl.LOOP_TYPES.add("changed"));
    assertThrows(UnsupportedOperationException.class, () -> LoopControl.LOOP_TYPE_DESCS.add("changed"));
  }

  @Test
  public void loopTypeArrayAccessorsReturnCopies() {
    final String[] types = LoopControl.loopTypes();
    types[0] = "changed";

    assertEquals(LoopControl.LOOP_COUNTED, LoopControl.loopTypes()[0]);
  }

  @Test
  public void convertsBetweenLoopTypesAndDescriptions() {
    assertEquals(LoopControl.LOOP_COUNTED, LoopControl.loopDescToType(LoopControl.LOOP_COUNTED));
    assertEquals(LoopControl.LOOP_WHILE, LoopControl.loopDescToType(LoopControl.LOOP_TYPE_DESCS.get(2)));
    assertEquals(LoopControl.LOOP_TYPE_DESCS.get(1), LoopControl.loopTypeToDesc(LoopControl.LOOP_UNTIL));
  }

  @Test
  public void fallsBackToCountedLoopForUnknownValues() {
    assertEquals(LoopControl.LOOP_COUNTED, LoopControl.loopDescToType("not-a-loop"));
    assertEquals(LoopControl.LOOP_TYPE_DESCS.get(0), LoopControl.loopTypeToDesc("not-a-loop"));
  }
}
