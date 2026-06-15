package VASSAL.build.module;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class InventoryTest {
  @Test
  public void numericalSortKeyExtractsSignedIntegers() {
    assertEquals(-42, Inventory.firstSignedInteger("piece -42"));
  }

  @Test
  public void numericalSortKeyClampsIntegerMinValue() {
    assertEquals(Integer.MIN_VALUE, Inventory.firstSignedInteger("-2147483648"));
  }

  @Test
  public void numericalSortKeyClampsPositiveOverflow() {
    assertEquals(Integer.MAX_VALUE, Inventory.firstSignedInteger("2147483648"));
  }

  @Test
  public void numericalSortKeyClampsValuesTooLargeForLong() {
    assertEquals(Integer.MAX_VALUE, Inventory.firstSignedInteger("999999999999999999999999"));
    assertEquals(Integer.MIN_VALUE, Inventory.firstSignedInteger("-999999999999999999999999"));
  }

  @Test
  public void numericalSortKeyUsesMinimumWhenNoIntegerExists() {
    assertEquals(Integer.MIN_VALUE, Inventory.firstSignedInteger("no number"));
  }
}
