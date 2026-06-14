package bsh;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BSHTryStatementTest {
  @Test
  public void typedCatchSkipsUnassignableExceptionTypes() throws EvalError {
    final Interpreter interpreter = new Interpreter();

    assertEquals(
      "bad",
      interpreter.eval(
        "result = null;" +
        "try {" +
        "  throw new java.lang.IllegalArgumentException(\"bad\");" +
        "}" +
        "catch (java.io.IOException e) {" +
        "  result = \"io\";" +
        "}" +
        "catch (java.lang.RuntimeException e) {" +
        "  result = e.getMessage();" +
        "}" +
        "result;"
      )
    );
  }
}
