package VASSAL.command;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import VASSAL.build.GameModule;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class ConditionalCommandTest {
  private static final String VERSION_PROPERTY = "version";

  @Test
  public void lessThanComparesGameModuleAttributeAsVersion() {
    assertThat(isLessThanSatisfied("3.7.0", "3.8.0"), is(true));
    assertThat(isLessThanSatisfied("3.8.0", "3.8.0"), is(false));
    assertThat(isLessThanSatisfied("3.9.0", "3.8.0"), is(false));
  }

  @Test
  public void greaterThanComparesGameModuleAttributeAsVersion() {
    assertThat(isGreaterThanSatisfied("3.9.0", "3.8.0"), is(true));
    assertThat(isGreaterThanSatisfied("3.8.0", "3.8.0"), is(false));
    assertThat(isGreaterThanSatisfied("3.7.0", "3.8.0"), is(false));
  }

  private boolean isLessThanSatisfied(String moduleValue, String conditionValue) {
    return withGameModuleVersion(
      moduleValue,
      () -> new ConditionalCommand.Lt(VERSION_PROPERTY, conditionValue).isSatisfied());
  }

  private boolean isGreaterThanSatisfied(String moduleValue, String conditionValue) {
    return withGameModuleVersion(
      moduleValue,
      () -> new ConditionalCommand.Gt(VERSION_PROPERTY, conditionValue).isSatisfied());
  }

  private boolean withGameModuleVersion(
    String version,
    java.util.function.Supplier<Boolean> conditionSupplier) {

    final GameModule gameModule = mock(GameModule.class);
    when(gameModule.getAttributeValueString(VERSION_PROPERTY)).thenReturn(version);

    try (MockedStatic<GameModule> staticGameModule = Mockito.mockStatic(GameModule.class)) {
      staticGameModule.when(GameModule::getGameModule).thenReturn(gameModule);
      return conditionSupplier.get();
    }
  }
}
