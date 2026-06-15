package VASSAL.build.module.documentation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class BrowserHelpFileTest {
  @Test
  public void copyHelpEntryCopiesBytes() throws IOException {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();

    BrowserHelpFile.copyHelpEntry(
      new ByteArrayInputStream(new byte[] {1, 2, 3}),
      out,
      Path.of("help/index.html")
    );

    assertArrayEquals(new byte[] {1, 2, 3}, out.toByteArray());
  }

  @Test
  public void copyHelpEntryReportsReadErrors() {
    final IOException e = assertThrows(
      IOException.class,
      () -> BrowserHelpFile.copyHelpEntry(
        new FailingInputStream(),
        new ByteArrayOutputStream(),
        Path.of("help/index.html")
      )
    );

    assertTrue(e.getMessage().startsWith("Error reading zipped help content for: help/index.html"));
    assertEquals("read failed", e.getCause().getMessage());
  }

  @Test
  public void copyHelpEntryReportsWriteErrors() {
    final IOException e = assertThrows(
      IOException.class,
      () -> BrowserHelpFile.copyHelpEntry(
        new ByteArrayInputStream(new byte[] {1}),
        new FailingOutputStream(),
        Path.of("help/index.html")
      )
    );

    assertTrue(e.getMessage().startsWith("Error writing extracted help file: help/index.html"));
    assertEquals("write failed", e.getCause().getMessage());
  }

  private static class FailingInputStream extends InputStream {
    @Override
    public int read() throws IOException {
      throw new IOException("read failed");
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      throw new IOException("read failed");
    }
  }

  private static class FailingOutputStream extends OutputStream {
    @Override
    public void write(int b) throws IOException {
      throw new IOException("write failed");
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      throw new IOException("write failed");
    }
  }
}
