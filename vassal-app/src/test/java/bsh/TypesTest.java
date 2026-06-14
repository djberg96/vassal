package bsh;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class TypesTest {
  @Test
  public void boxesPrimitiveToWrapperType() throws UtilEvalError {
    final Object result = Types.castObject(
      new Primitive(7), Integer.class, Types.ASSIGNMENT);

    assertThat(result, is(7));
  }

  @Test
  public void boxesPrimitiveToObjectType() throws UtilEvalError {
    final Object result = Types.castObject(
      new Primitive(7), Object.class, Types.ASSIGNMENT);

    assertThat(result, is(7));
  }

  @Test
  public void castsScriptedObjectToInterfaceProxy() throws UtilEvalError {
    final NameSpace namespace =
      new NameSpace((BshClassManager)null, "test scripted object");
    final This scriptedObject = This.getThis(namespace, new Interpreter());

    final Object result = Types.castObject(
      scriptedObject, ScriptedObjectInterface.class, Types.ASSIGNMENT);

    assertThat(result, instanceOf(ScriptedObjectInterface.class));
  }

  private interface ScriptedObjectInterface {
    void call();
  }
}
