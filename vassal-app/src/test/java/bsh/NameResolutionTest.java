package bsh;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameResolutionTest {
  @Test
  public void scriptedClassReturnsClassInstanceForThisFromBlock()
    throws EvalError {
    final Interpreter interpreter = new Interpreter();

    assertEquals(
      "Child",
      interpreter.eval(
        "class Child {" +
        "  public Object self() {" +
        "    if (true) { return this; }" +
        "    return null;" +
        "  }" +
        "}" +
        "new Child().self().getClass().getName();"
      )
    );
  }

  @Test
  public void scriptedClassCanCallOwnMethodThroughThisFromBlock()
    throws EvalError {
    final Interpreter interpreter = new Interpreter();

    assertEquals(
      "child",
      interpreter.eval(
        "class Child {" +
        "  public String label() { return \"child\"; }" +
        "  public String nestedLabel() {" +
        "    if (true) { return this.label(); }" +
        "    return \"unreachable\";" +
        "  }" +
        "}" +
        "new Child().nestedLabel();"
      )
    );
  }

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
