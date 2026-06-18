package bsh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class MethodInvocationTest {
  @Test
  public void objectMethodInvocationPreservesTargetException() throws EvalError {
    final Interpreter interpreter = new Interpreter();
    interpreter.set("helper", new ThrowingHelper());

    final TargetError error = assertThrows(
      TargetError.class,
      () -> interpreter.eval("helper.fail();")
    );

    assertInstanceOf(IllegalStateException.class, error.getTarget());
    assertEquals("boom", error.getTarget().getMessage());
  }

  public static final class ThrowingHelper {
    public void fail() {
      throw new IllegalStateException("boom");
    }
  }
}
