package org.netbeans.spi.wizard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

class SimpleWizardInfoTest {
  private static class TestProvider extends WizardPanelProvider {
    private int finishCount = 0;

    private TestProvider(String[] steps) {
      super("Test Wizard", steps, steps);
    }

    @Override
    protected JComponent createPanel(WizardController controller, String id, Map<Object, Object> settings) {
      return new JPanel();
    }

    @Override
    protected Object finish(Map<Object, Object> settings) {
      ++finishCount;
      return "finished";
    }
  }

  @Test
  void finishAllowsLastStepDefaultNavigationMode() throws WizardException {
    final TestProvider provider = new TestProvider(new String[] {"first", "last"});
    final SimpleWizard wizard = new SimpleWizard(provider);
    final Map<Object, Object> settings = new HashMap<>();

    wizard.navigatingTo("last", settings);

    assertEquals("finished", wizard.finish(settings));
    assertEquals(1, provider.finishCount);
  }

  @Test
  void finishRejectsCurrentProblem() {
    final TestProvider provider = new TestProvider(new String[] {"only"});
    final SimpleWizardInfo info = new SimpleWizardInfo(provider);
    final SimpleWizard wizard = new SimpleWizard(info);
    final Map<Object, Object> settings = new HashMap<>();

    wizard.navigatingTo("only", settings);
    info.setProblem("Required value missing");

    assertThrows(IllegalStateException.class, () -> wizard.finish(settings));
    assertEquals(0, provider.finishCount);
  }

  @Test
  void finishRejectsIntermediateStep() {
    final TestProvider provider = new TestProvider(new String[] {"first", "last"});
    final SimpleWizard wizard = new SimpleWizard(provider);
    final Map<Object, Object> settings = new HashMap<>();

    wizard.navigatingTo("first", settings);

    assertThrows(IllegalStateException.class, () -> wizard.finish(settings));
    assertEquals(0, provider.finishCount);
  }
}
