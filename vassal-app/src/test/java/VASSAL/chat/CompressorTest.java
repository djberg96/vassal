package VASSAL.chat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

public class CompressorTest {

  @Test
  public void roundTripsEmptyPayload() throws IOException {
    assertRoundTrip(new byte[0]);
  }

  @Test
  public void roundTripsUtf8Text() throws IOException {
    assertRoundTrip("hello, VASSAL \u2603".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  public void roundTripsBinaryPayload() throws IOException {
    final byte[] payload = new byte[256];
    for (int i = 0; i < payload.length; ++i) {
      payload[i] = (byte) i;
    }

    assertRoundTrip(payload);
  }

  @Test
  public void producesZipPayload() throws IOException {
    final byte[] compressed = Compressor.compress("payload".getBytes(StandardCharsets.UTF_8));

    assertEquals('P', compressed[0]);
    assertEquals('K', compressed[1]);
  }

  @Test
  public void rejectsInvalidPayload() {
    assertThrows(
      IOException.class,
      () -> Compressor.decompress("not a zip payload".getBytes(StandardCharsets.UTF_8))
    );
  }

  private static void assertRoundTrip(byte[] payload) throws IOException {
    final byte[] compressed = Compressor.compress(payload);

    assertArrayEquals(payload, Compressor.decompress(compressed));
    assertEquals('P', compressed[0]);
    assertEquals('K', compressed[1]);
  }
}
