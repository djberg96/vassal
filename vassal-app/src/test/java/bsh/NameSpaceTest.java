package bsh;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotSame;

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

  private static BshClassManager storedClassManager(NameSpace namespace)
    throws ReflectiveOperationException {
    final Field field = NameSpace.class.getDeclaredField("classManager");
    field.setAccessible(true);
    return (BshClassManager) field.get(namespace);
  }
}
