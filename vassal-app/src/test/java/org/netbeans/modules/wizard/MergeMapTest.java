package org.netbeans.modules.wizard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class MergeMapTest {
  @Test
  void usesCurrentStepForWritesAndReadsVisibleStackTopFirst() {
    final MergeMap map = new MergeMap("one");
    map.put("shared", "one");
    map.put("oneOnly", 1);

    map.push("two");
    map.put("shared", "two");
    map.put("twoOnly", 2);

    assertEquals("two", map.get("shared"));
    assertEquals(1, map.get("oneOnly"));
    assertEquals(2, map.get("twoOnly"));
    assertEquals(3, map.size());
    assertTrue(map.containsKey("shared"));
    assertTrue(map.containsValue("two"));
  }

  @Test
  void popAndCalveHidesCurrentStepAndRestoresItWhenPushedAgain() {
    final MergeMap map = new MergeMap("one");
    map.put("shared", "one");
    map.put("oneOnly", 1);

    map.push("two");
    map.put("shared", "two");
    map.put("twoOnly", 2);

    assertEquals("two", map.popAndCalve());
    assertEquals("one", map.currID());
    assertEquals("two", map.get("shared"));
    assertEquals(1, map.get("oneOnly"));
    assertFalse(map.containsKey("twoOnly"));

    map.push("two");
    assertEquals("two", map.get("shared"));
    assertEquals(2, map.get("twoOnly"));
  }

  @Test
  void removeDeletesDuplicateKeysFromAllStackedMaps() {
    final MergeMap map = new MergeMap("one");
    map.put("shared", "one");

    map.push("two");
    map.put("shared", "two");

    assertEquals("two", map.remove("shared"));
    assertFalse(map.containsKey("shared"));

    map.popAndCalve();
    map.push("two");
    assertFalse(map.containsKey("shared"));
  }

  @Test
  void rejectsDuplicateActiveStep() {
    final MergeMap map = new MergeMap("one");

    assertThrows(RuntimeException.class, () -> map.push("one"));
  }

  @Test
  void keepsEverpresentMapAsMutableBackingMap() {
    final Map<Object, Object> base = new HashMap<>();
    base.put("base", "initial");

    final MergeMap map = new MergeMap("one", base);
    assertEquals("initial", map.get("base"));

    base.put("late", "added");
    assertEquals("added", map.get("late"));

    map.put("base", "current");
    assertEquals("current", map.get("base"));
    assertEquals("initial", base.get("base"));

    assertEquals("one", map.popAndCalve());
    assertEquals("current", map.get("base"));
    assertEquals("current", base.get("base"));

    base.put("base", "mutated");
    assertEquals("mutated", map.get("base"));

    map.push("one");
    assertEquals("current", map.get("base"));
  }
}
