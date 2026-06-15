package VASSAL.build.module.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

public class TutorialTest {
  @Test
  public void unableToLaunchMessageIncludesCauseMessage() {
    assertEquals(
      "Unable to launch tutorial Demo:  missing tutorial log",
      Tutorial.unableToLaunchMessage(
        "Demo",
        new ExecutionException(new IOException("missing tutorial log"))
      )
    );
  }

  @Test
  public void unableToLaunchMessageFallsBackToExecutionExceptionMessage() {
    assertEquals(
      "Unable to launch tutorial Demo:  worker failed",
      Tutorial.unableToLaunchMessage(
        "Demo",
        new ExecutionException("worker failed", null)
      )
    );
  }

  @Test
  public void unableToLaunchMessageOmitsBlankDetail() {
    assertEquals(
      "Unable to launch tutorial Demo",
      Tutorial.unableToLaunchMessage(
        "Demo",
        new ExecutionException(new IOException(" "))
      )
    );
  }
}
