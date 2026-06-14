package bsh;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BSHAllocationExpressionTest {
  @Test
  public void anonymousInterfaceBodyCanImplementMethod() throws EvalError {
    final Interpreter interpreter = new Interpreter();

    assertEquals(
      "supplied",
      interpreter.eval(
        "supplier = new java.util.function.Supplier() {" +
        "  public Object get() { return \"supplied\"; }" +
        "};" +
        "supplier.get();"
      )
    );
  }

  @Test
  public void anonymousConcreteClassBodyCanOverrideMethod() throws EvalError {
    final Interpreter interpreter = new Interpreter();

    assertEquals(
      "anon",
      interpreter.eval(
        "obj = new java.lang.Object() {" +
        "  public String toString() { return \"anon\"; }" +
        "};" +
        "obj.toString();"
      )
    );
  }

  @Test
  public void anonymousConcreteClassBodyCanUseInstanceFields() throws EvalError {
    final Interpreter interpreter = new Interpreter();

    assertEquals(
      42,
      interpreter.eval(
        "obj = new java.lang.Object() {" +
        "  int x = 41;" +
        "  public int getX() { return x + 1; }" +
        "};" +
        "obj.getX();"
      )
    );
  }

  @Test
  public void anonymousConcreteClassBodyCanUseEnclosingVariables() throws EvalError {
    final Interpreter interpreter = new Interpreter();

    assertEquals(
      7,
      interpreter.eval(
        "base = 5;" +
        "obj = new java.lang.Object() {" +
        "  public int getX() { return base + 2; }" +
        "};" +
        "obj.getX();"
      )
    );
  }
}
