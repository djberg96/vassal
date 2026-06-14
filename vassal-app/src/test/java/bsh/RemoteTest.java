package bsh;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RemoteTest {
  @TempDir
  private Path tempDir;

  @Test
  public void buildFormDataEncodesScriptAsUtf8() {
    final String script = "print(\"cafe \u00e9 \u96ea\");";
    final String prefix = "bsh.client=Remote&bsh.script=";
    final String formData = Remote.buildFormData(script);

    assertTrue(formData.startsWith(prefix));
    assertEquals(
      script,
      URLDecoder.decode(formData.substring(prefix.length()), StandardCharsets.UTF_8)
    );
  }

  @Test
  public void getFileReadsUtf8Text() throws IOException {
    final String script = "print(\"cafe \u00e9 \u96ea\");\n";
    final Path file = tempDir.resolve("script.bsh");
    Files.writeString(file, script, StandardCharsets.UTF_8);

    assertEquals(script, Remote.getFile(file.toString()));
  }
}
