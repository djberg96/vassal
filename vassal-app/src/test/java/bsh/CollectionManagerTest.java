package bsh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CollectionManagerTest {

  private final CollectionManager manager = CollectionManager.getCollectionManager();

  @Test
  public void iteratesOverEnumeration() {
    final Vector<String> values = new Vector<>(List.of("a", "b"));

    assertThat(toList(manager.getBshIterator(values.elements())), contains("a", "b"));
  }

  @Test
  public void returnsIteratorObjectsDirectly() {
    final Iterator<String> iterator = List.of("a", "b").iterator();

    assertThat(manager.getBshIterator(iterator), is(iterator));
  }

  @Test
  public void iteratesOverIterable() {
    assertThat(toList(manager.getBshIterator(List.of("a", "b"))), contains("a", "b"));
  }

  @Test
  public void iteratesOverArray() {
    assertThat(toList(manager.getBshIterator(new int[] { 1, 2 })), contains(1, 2));
  }

  @Test
  public void iteratesOverCharactersInCharSequence() {
    assertThat(toList(manager.getBshIterator("ab")), contains('a', 'b'));
  }

  @Test
  public void rejectsNullIterationTargets() {
    assertThrows(NullPointerException.class, () -> manager.getBshIterator(null));
  }

  @Test
  public void readsAndWritesMaps() {
    final Map<String, Object> values = new HashMap<>();
    values.put("one", 1);

    assertThat(manager.isMap(values), is(true));
    assertThat(manager.getFromMap(values, "one"), is(1));
    assertThat(manager.putInMap(values, "one", 2), is(1));
    assertThat(manager.getFromMap(values, "one"), is(2));
  }

  private static List<Object> toList(Iterator<?> iterator) {
    final List<Object> values = new ArrayList<>();
    iterator.forEachRemaining(values::add);
    return values;
  }
}
