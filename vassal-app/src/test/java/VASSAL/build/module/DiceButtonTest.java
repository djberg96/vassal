package VASSAL.build.module;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class DiceButtonTest {

  @Test
  public void propertyNamesUseConfiguredName() {
    final DiceButton button = new DiceButton() {
      @Override
      protected void initLaunchButton() {
      }
    };
    button.setConfigureName("Attack");

    assertEquals(
      List.of("Attack_result", "Attack_total", "Attack_keep", "Attack_summary"),
      button.getPropertyNames()
    );
  }
}
