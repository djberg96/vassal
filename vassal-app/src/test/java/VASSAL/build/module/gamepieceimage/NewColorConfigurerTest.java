package VASSAL.build.module.gamepieceimage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Color;

import org.junit.jupiter.api.Test;

public class NewColorConfigurerTest {
  @Test
  public void stringToColorParsesRgb() {
    assertEquals(new Color(1, 2, 3), NewColorConfigurer.stringToColor("1,2,3"));
  }

  @Test
  public void stringToColorPreservesNullSentinel() {
    assertNull(NewColorConfigurer.stringToColor(null));
    assertNull(NewColorConfigurer.stringToColor("null"));
  }

  @Test
  public void stringToColorFallsBackToBlackForNonNumericData() {
    assertEquals(Color.BLACK, NewColorConfigurer.stringToColor("red,2,3"));
  }

  @Test
  public void stringToColorFallsBackToBlackForMissingComponents() {
    assertEquals(Color.BLACK, NewColorConfigurer.stringToColor("1,2"));
  }
}
