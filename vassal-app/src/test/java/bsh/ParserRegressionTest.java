package bsh;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParserRegressionTest {
  @Test
  public void regularForLoopSupportsTypedInitializerAndUpdate() throws EvalError {
    final Interpreter interpreter = new Interpreter();

    assertEquals(
      10,
      interpreter.eval(
        "total = 0;" +
        "for (int i = 0; i < 5; i++) {" +
        "  total = total + i;" +
        "}" +
        "total;"
      )
    );
  }

  @Test
  public void enhancedForLoopSupportsLooseVariableDeclaration() throws EvalError {
    final Interpreter interpreter = new Interpreter();

    assertEquals(
      "abc",
      interpreter.eval(
        "items = new String[] { \"a\", \"b\", \"c\" };" +
        "result = \"\";" +
        "for (item : items) {" +
        "  result = result + item;" +
        "}" +
        "result;"
      )
    );
  }

  @Test
  public void enhancedForLoopSupportsTypedVariableDeclaration() throws EvalError {
    final Interpreter interpreter = new Interpreter();

    assertEquals(
      "ABC",
      interpreter.eval(
        "items = new String[] { \"a\", \"b\", \"c\" };" +
        "result = \"\";" +
        "for (String item : items) {" +
        "  result = result + item.toUpperCase();" +
        "}" +
        "result;"
      )
    );
  }

  @Test
  public void typedVariableDeclarationSupportsModifiersAndMultipleDeclarators()
    throws EvalError {
    final Interpreter interpreter = new Interpreter();

    assertEquals(
      9,
      interpreter.eval(
        "final int first = 4, second = 5;" +
        "first + second;"
      )
    );
  }

  @Test
  public void staticImportCanResolveJavaMembers() throws EvalError {
    final Interpreter interpreter = new Interpreter();

    assertEquals(
      5,
      interpreter.eval(
        "static import java.lang.Math.*;" +
        "(int) max(3, 5);"
      )
    );
  }

  @Test
  public void parseErrorsIncludeLineAndColumn() {
    final Interpreter interpreter = new Interpreter();

    final EvalError error = assertThrows(
      EvalError.class,
      () -> interpreter.eval(
        "value = 1;\n" +
        "broken = ;"
      )
    );

    assertTrue(error.getMessage().contains("line 2"));
    assertTrue(error.getMessage().contains("column"));
  }
}
