package VASSAL.counters;

import java.lang.reflect.Constructor;

import VASSAL.build.MockModuleTest;

public abstract class SerializeTest<T extends Decorator> extends MockModuleTest {
  public void serializeTest(Class<T>clazz, T trait) throws Exception {
    BasicPiece basicPiece = new BasicPiece();
    trait.setInner(basicPiece);
    String typeString = trait.myGetType();

    T deserialized = deserialize(clazz, typeString);
    assertSame(trait, deserialized);
  }

  private T deserialize(Class<T> clazz, String typeString) throws Exception {
    try {
      final T deserialized = clazz.getConstructor().newInstance();
      deserialized.mySetType(typeString);
      return deserialized;
    }
    catch (NoSuchMethodException e) {
      return deserializeWithLegacyConstructor(clazz, typeString);
    }
  }

  private T deserializeWithLegacyConstructor(Class<T> clazz, String typeString) throws Exception {
    Constructor<T> constructor;

    try {
      constructor = clazz.getConstructor(String.class, GamePiece.class);
      return constructor.newInstance(typeString, null);
    }
    catch (NoSuchMethodException e) {
      constructor = clazz.getConstructor(GamePiece.class, String.class);
      return constructor.newInstance(null, typeString);
    }
  }

  abstract void assertSame(T t1, T t2);
}
