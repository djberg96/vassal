package VASSAL.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AbstractInterpreterTest {
  @Test
  public void describeAssignmentValueReportsNull() {
    assertEquals("null", AbstractInterpreter.describeAssignmentValue(null));
  }

  @Test
  public void describeAssignmentValueReportsValueAndType() {
    assertEquals("'42' (java.lang.Integer)", AbstractInterpreter.describeAssignmentValue(42));
  }

  @Test
  public void describeTypedAssignmentValueReportsDeclaredType() {
    assertEquals(
      "typed as java.lang.Number with '42' (java.lang.Integer)",
      AbstractInterpreter.describeTypedAssignmentValue(Number.class, 42)
    );
  }
}
