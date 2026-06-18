package VASSAL.configure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import VASSAL.tools.ArchiveWriter;

public class FileConfigurerTest {
  @TempDir
  File tempDir;

  @Test
  public void archiveBackedConfigurerPreservesArchiveEntryName() {
    final ArchiveWriter archive = mock(ArchiveWriter.class);
    final FileConfigurer configurer = new FileConfigurer("file", "File", archive);

    configurer.setValue("inside-module.dat");

    assertEquals("inside-module.dat", configurer.getValueString());
    assertFalse(configurer.getFileValue().exists());
    verify(archive, never()).addFile("inside-module.dat", "inside-module.dat");
  }

  @Test
  public void archiveBackedConfigurerCopiesExistingLocalFile() throws IOException {
    final ArchiveWriter archive = mock(ArchiveWriter.class);
    final FileConfigurer configurer = new FileConfigurer("file", "File", archive);
    final File file = new File(tempDir, "local.dat");
    Files.writeString(file.toPath(), "content");

    configurer.setValue(file);

    assertEquals("local.dat", configurer.getValueString());
    verify(archive).addFile(file.getPath(), "local.dat");
  }

  @Test
  public void nonArchiveConfigurerPreservesLocalPath() {
    final FileConfigurer configurer = new FileConfigurer("file", "File");
    final File file = new File(tempDir, "local.dat");

    configurer.setValue(file);

    assertEquals(file.getPath(), configurer.getValueString());
  }
}
