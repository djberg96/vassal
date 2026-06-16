package VASSAL.counters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class KeyBufferTest {
  private final KeyBuffer buffer = KeyBuffer.getBuffer();

  @AfterEach
  void clearBuffer() {
    buffer.clear();
  }

  @Test
  void addKeepsSelectionOrderAndIgnoresDuplicates() {
    final GamePiece first = new BasicPiece();
    final GamePiece second = new BasicPiece();

    buffer.add(first);
    buffer.add(second);
    buffer.add(first);

    assertEquals(
      java.util.List.of(first, second),
      buffer.asList()
    );
    assertTrue(buffer.contains(first));
    assertTrue(buffer.contains(second));
  }

  @Test
  void removeUpdatesMembership() {
    final GamePiece piece = new BasicPiece();

    buffer.add(piece);
    buffer.remove(piece);

    assertFalse(buffer.contains(piece));
    assertTrue(buffer.isEmpty());
  }
}
