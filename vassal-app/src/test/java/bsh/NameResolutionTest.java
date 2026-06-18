package bsh;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameResolutionTest {
  @Test
  public void scriptedClassCanCallSuperclassMethodFromBlock()
    throws EvalError {
    final Interpreter interpreter = new Interpreter();

    assertEquals(
      "parent",
      interpreter.eval(
        "class Parent {" +
        "  public String label() { return \"parent\"; }" +
        "}" +
        "class Child extends Parent {" +
        "  public String label() { return \"child\"; }" +
        "  public String parentLabel() {" +
        "    if (true) { return super.label(); }" +
        "    return \"unreachable\";" +
        "  }" +
        "}" +
        "new Child().parentLabel();"
      )
    );
  }
}
