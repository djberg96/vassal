package bsh;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NameSpaceTest {
  @Test
  public void childNamespaceUsesParentClassManagerBeforePruning() {
    final BshClassManager parentClassManager =
      BshClassManager.createClassManager(null);
    final NameSpace parent = new NameSpace(parentClassManager, "parent");
    final NameSpace child = new NameSpace(parent, "child");

    assertSame(parentClassManager, child.getClassManager());
  }

  @Test
  public void pruneDetachesFromParentAndCreatesClassManagerLazily()
    throws ReflectiveOperationException {
    final BshClassManager parentClassManager =
      BshClassManager.createClassManager(null);
    final NameSpace parent = new NameSpace(parentClassManager, "parent");
    final NameSpace child = new NameSpace(parent, "child");

    child.prune();

    assertNull(storedClassManager(child));

    final BshClassManager prunedClassManager = child.getClassManager();
    assertNotNull(prunedClassManager);
    assertNotSame(parentClassManager, prunedClassManager);
    assertSame(prunedClassManager, storedClassManager(child));
  }

  @Test
  public void pruneKeepsExistingClassManager()
    throws ReflectiveOperationException {
    final BshClassManager classManager =
      BshClassManager.createClassManager(null);
    final NameSpace namespace = new NameSpace(classManager, "owned");

    namespace.prune();

    assertSame(classManager, storedClassManager(namespace));
    assertSame(classManager, namespace.getClassManager());
  }

  @Test
  public void serializationSkipsImportedClassInstanceObjects() {
    final NameSpace namespace = new NameSpace(
      (BshClassManager) null,
      "class instance namespace"
    );
    namespace.setClassInstance(new NonSerializableClassInstance());

    assertDoesNotThrow(() -> {
      try (
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes)
      ) {
        out.writeObject(namespace);
      }
    });
  }

  @Test
  public void externalNamespaceSerializesMapSnapshot()
    throws Exception {
    final Map<String,Object> map = new NonSerializableMap();
    final ExternalNameSpace namespace =
      new ExternalNameSpace(null, "external", map);
    namespace.setVariable("score", 7, false, false);

    final ExternalNameSpace copy = serializeAndDeserialize(namespace);

    assertEquals(7, copy.getMap().get("score"));
  }

  @Test
  public void externalNamespaceSetMapAcceptsNull() {
    final ExternalNameSpace namespace = new ExternalNameSpace();

    namespace.setMap(null);

    assertNotNull(namespace.getMap());
  }

  @Test
  public void externalNamespaceAllNamesIncludesMapNames()
    throws UtilEvalError {
    final Map<String,Object> map = new HashMap<>();
    map.put("fromMap", 1);
    final ExternalNameSpace namespace =
      new ExternalNameSpace(null, "external", map);
    namespace.setVariable("fromNamespace", 2, false, false);

    final Set<String> names = Set.copyOf(Arrays.asList(namespace.getAllNames()));

    assertTrue(names.contains("fromMap"));
    assertTrue(names.contains("fromNamespace"));
  }

  @Test
  public void externalNamespaceAllNamesIncludesParentNames()
    throws UtilEvalError {
    final NameSpace parent = new NameSpace((BshClassManager) null, "parent");
    parent.setVariable("fromParent", 1, false, false);
    final Map<String,Object> map = new HashMap<>();
    map.put("fromMap", 2);
    final ExternalNameSpace namespace =
      new ExternalNameSpace(parent, "external", map);

    final Set<String> names = Set.copyOf(Arrays.asList(namespace.getAllNames()));

    assertTrue(names.contains("fromMap"));
    assertTrue(names.contains("fromParent"));
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

  private static BshClassManager storedClassManager(NameSpace namespace)
    throws ReflectiveOperationException {
    final Field field = NameSpace.class.getDeclaredField("classManager");
    field.setAccessible(true);
    return (BshClassManager) field.get(namespace);
  }

  private static final class NonSerializableClassInstance {
  }

  private static final class NonSerializableMap
    extends AbstractMap<String,Object> {
    private final Map<String,Object> backing = new HashMap<>();

    @Override
    public Object put(String key, Object value) {
      return backing.put(key, value);
    }

    @Override
    public Set<Entry<String,Object>> entrySet() {
      return backing.entrySet();
    }
  }
}
