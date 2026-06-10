package VASSAL.configure;

import VASSAL.counters.GlobalCommandTarget;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class GlobalCommandTargetConfigurerTest {

  @Test
  public void defaultConstructorUsesDefaultTarget() {
    final GlobalCommandTargetConfigurer configurer =
      new GlobalCommandTargetConfigurer("key", "name");

    assertNotNull(configurer.getTarget());
    assertEquals(new GlobalCommandTarget().encode(), configurer.getValueString());
  }

  @Test
  public void constructorCopiesSuppliedTarget() {
    final GlobalCommandTarget target = new GlobalCommandTarget();
    target.setFastMatchProperty(true);
    target.setTargetProperty("Side");
    target.setTargetValue("Allied");

    final GlobalCommandTargetConfigurer configurer =
      new GlobalCommandTargetConfigurer("key", "name", target);

    assertNotSame(target, configurer.getTarget());
    assertEquals(target.encode(), configurer.getValueString());
  }
}
