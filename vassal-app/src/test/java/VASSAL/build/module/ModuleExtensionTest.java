package VASSAL.build.module;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class ModuleExtensionTest {
  @Test
  public void malformedNextPieceSlotIdKeepsExistingSequence() {
    final ModuleExtension extension = new ModuleExtension(null);
    extension.setAttribute(ModuleExtension.EXTENSION_ID, "ext");
    extension.setAttribute(ModuleExtension.NEXT_PIECESLOT_ID, "7");

    extension.setAttribute(ModuleExtension.NEXT_PIECESLOT_ID, "not-a-number");

    assertThat(extension.generateGpId(), is(equalTo("ext:7")));
    assertThat(extension.generateGpId(), is(equalTo("ext:8")));
  }
}
