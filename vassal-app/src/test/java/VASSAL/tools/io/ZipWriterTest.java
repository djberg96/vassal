package VASSAL.tools.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ZipWriterTest {
  @TempDir
  Path tempDir;

  @Test
  public void writesZipEntriesAndReleasesFileLock() throws IOException {
    final Path zipPath = tempDir.resolve("test.zip");

    try (ZipWriter writer = new ZipWriter(zipPath)) {
      writer.write("hello".getBytes(StandardCharsets.UTF_8), "test.txt");
    }

    try (ZipFile zip = new ZipFile(zipPath.toFile())) {
      final byte[] content =
        zip.getInputStream(zip.getEntry("test.txt")).readAllBytes();
      assertThat(
        new String(content, StandardCharsets.UTF_8),
        is(equalTo("hello"))
      );
    }

    try (FileChannel channel = FileChannel.open(zipPath, StandardOpenOption.WRITE);
         FileLock lock = channel.tryLock()) {
      assertNotNull(lock);
    }
  }
}
