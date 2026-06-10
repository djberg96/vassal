package VASSAL.tools.imageop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScaleOpTiledBitmapImplTest {

  @Test
  public void integralPowersOfTwoAreRecognized() {
    assertTrue(ScaleOpTiledBitmapImpl.isIntegralPowerOfTwo(1.0));
    assertTrue(ScaleOpTiledBitmapImpl.isIntegralPowerOfTwo(2.0));
    assertTrue(ScaleOpTiledBitmapImpl.isIntegralPowerOfTwo(4.0));
  }

  @Test
  public void nonIntegralOrNonPowerOfTwoValuesAreRejected() {
    assertFalse(ScaleOpTiledBitmapImpl.isIntegralPowerOfTwo(0.0));
    assertFalse(ScaleOpTiledBitmapImpl.isIntegralPowerOfTwo(3.0));
    assertFalse(ScaleOpTiledBitmapImpl.isIntegralPowerOfTwo(2.5));
  }
}
