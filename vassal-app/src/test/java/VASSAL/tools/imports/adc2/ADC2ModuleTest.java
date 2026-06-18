package VASSAL.tools.imports.adc2;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import org.junit.jupiter.api.Test;

class ADC2ModuleTest {
  @Test
  void readPieceStatusDotsBlockConsumesIgnoredDefinitions() throws Exception {
    try (DataInputStream in = streamStatusDotsBlock()) {
      assertDoesNotThrow(() -> new ADC2Module().readPieceStatusDotsBlock(in));
      assertEquals(0, in.available());
    }
  }

  private static DataInputStream streamStatusDotsBlock() {
    final byte[] data = new byte[1 + 6 * 8];
    int offset = 0;
    data[offset++] = (byte) ADC2Utils.BLOCK_SEPARATOR;

    for (int i = 0; i < 6; ++i) {
      data[offset++] = (byte) i; // type
      data[offset++] = 0; // show flags, high byte
      data[offset++] = (byte) i; // show flags, low byte
      data[offset++] = (byte) (i + 1); // color index
      data[offset++] = (byte) (i + 2); // position
      data[offset++] = (byte) (i + 3); // width
      data[offset++] = (byte) (i + 4); // height
      data[offset++] = (byte) (i + 5); // diameter
    }

    return new DataInputStream(new ByteArrayInputStream(data));
  }
}
