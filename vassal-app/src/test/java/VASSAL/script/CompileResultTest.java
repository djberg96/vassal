package VASSAL.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bsh.EvalError;
import bsh.ParseException;
import org.junit.jupiter.api.Test;

class CompileResultTest {
  @Test
  void noArgConstructorCreatesSuccessfulResult() {
    final CompileResult result = new CompileResult();

    assertTrue(result.isSuccess());
    assertEquals("", result.getMessage()); //NON-NLS
  }

  @Test
  void parseExceptionMessageIsReportedDirectly() {
    final CompileResult result = new CompileResult(new ParseException("bad syntax")); //NON-NLS

    assertFalse(result.isSuccess());
    assertEquals("bad syntax: <at unknown location>", result.getMessage()); //NON-NLS
  }

  @Test
  void evalErrorMessageUsesLineAndErrorText() {
    final CompileResult result = new CompileResult(new EvalError("boom", null, null)); //NON-NLS

    assertFalse(result.isSuccess());
    assertEquals("-1: <unknown error>", result.getMessage()); //NON-NLS
  }
}
