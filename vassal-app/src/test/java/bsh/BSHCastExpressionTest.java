package bsh;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BSHCastExpressionTest {
  @Test
  public void strictJavaAllowsExplicitPrimitiveNarrowingCast() throws EvalError {
    final Interpreter interpreter = new Interpreter();
    interpreter.setStrictJava(true);

    assertEquals(
      (byte) 1,
      interpreter.eval("(byte) 257;")
    );
  }

  @Test
  public void strictJavaRejectsBeanShellNumericWrapperCasts() {
    final Interpreter interpreter = new Interpreter();
    interpreter.setStrictJava(true);

    assertThrows(
      TargetError.class,
      () -> interpreter.eval("(Byte) Integer.valueOf(1);")
    );
  }

  @Test
  public void strictJavaAllowsReferenceDowncastSyntax() throws EvalError {
    final Interpreter interpreter = new Interpreter();
    interpreter.setStrictJava(true);

    assertEquals(
      "text",
      interpreter.eval("Object value = \"text\"; ((String) value);")
    );
  }
}
