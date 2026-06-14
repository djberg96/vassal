package bsh;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BshClassManagerTest {
  @Test
  public void associateClassStoresGeneratedClass() {
    final BshClassManager classManager =
      BshClassManager.createClassManager(null);

    classManager.associateClass(TestGeneratedClass.class);

    assertSame(
      TestGeneratedClass.class,
      classManager.getAssociatedClass(TestGeneratedClass.class.getName())
    );
  }

  @Test
  public void associateClassRejectsOrdinaryClass() {
    final BshClassManager classManager =
      BshClassManager.createClassManager(null);

    assertThrows(
      IllegalArgumentException.class,
      () -> classManager.associateClass(String.class)
    );
    assertNull(classManager.getAssociatedClass(String.class.getName()));
  }

  @Test
  public void associateClassRejectsNullClass() {
    final BshClassManager classManager =
      BshClassManager.createClassManager(null);

    assertThrows(
      IllegalArgumentException.class,
      () -> classManager.associateClass(null)
    );
  }

  private static class TestGeneratedClass implements GeneratedClass {
  }
}
