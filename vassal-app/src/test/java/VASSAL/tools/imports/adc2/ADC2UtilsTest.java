package VASSAL.tools.imports.adc2;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import org.junit.jupiter.api.Test;

import VASSAL.tools.imports.FileFormatException;

class ADC2UtilsTest {
  @Test
  void readBlockHeaderAcceptsBlockSeparatorWithPayload() {
    assertDoesNotThrow(() -> ADC2Utils.readBlockHeader(
      stream(ADC2Utils.BLOCK_SEPARATOR, 0),
      "Test Block"
    ));
  }

  @Test
  void readBlockHeaderRejectsUnexpectedHeaderByte() {
    final var thrown = assertThrows(
      FileFormatException.class,
      () -> ADC2Utils.readBlockHeader(stream(0, 1), "Test Block")
    );

    assertEquals("Invalid Test Block block header.", thrown.getMessage());
  }

  @Test
  void readBlockHeaderReportsMissingBlock() {
    final var thrown = assertThrows(
      ADC2Utils.NoMoreBlocksException.class,
      () -> ADC2Utils.readBlockHeader(stream(), "Test Block")
    );

    assertEquals("No more ADC2 blocks while reading Test Block.", thrown.getMessage());
  }

  private static DataInputStream stream(int... bytes) {
    final byte[] data = new byte[bytes.length];
    for (int i = 0; i < bytes.length; ++i) {
      data[i] = (byte) bytes[i];
    }
    return new DataInputStream(new ByteArrayInputStream(data));
  }
}
