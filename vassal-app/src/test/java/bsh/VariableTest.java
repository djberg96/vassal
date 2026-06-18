package bsh;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class VariableTest {
  @Test
  public void serializationPreservesSerializableValue()
    throws Exception {
    final Variable variable = new Variable("score", 7, null);

    final Variable copy = serializeAndDeserialize(variable);

    assertEquals(7, copy.getValue());
  }

  @Test
  public void serializationReplacesNonSerializableValueWithVoid()
    throws Exception {
    final Variable variable =
      new Variable("object", new NonSerializableValue(), null);

    final Variable copy = serializeAndDeserialize(variable);

    assertSame(Primitive.VOID, copy.getValue());
  }

  @Test
  public void serializationPreservesPrimitiveNullSentinel()
    throws Exception {
    final Variable variable = new Variable("nothing", Primitive.NULL, null);

    final Variable copy = serializeAndDeserialize(variable);

    assertSame(Primitive.NULL, copy.getValue());
  }

  private static <T> T serializeAndDeserialize(T value)
    throws Exception {
    final byte[] bytes;
    try (
      ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
      ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput)
    ) {
      objectOutput.writeObject(value);
      bytes = byteOutput.toByteArray();
    }

    try (
      ByteArrayInputStream byteInput = new ByteArrayInputStream(bytes);
      ObjectInputStream objectInput = new ObjectInputStream(byteInput)
    ) {
      @SuppressWarnings("unchecked")
      final T copy = (T) objectInput.readObject();
      return copy;
    }
  }

  private static final class NonSerializableValue {
  }
}
