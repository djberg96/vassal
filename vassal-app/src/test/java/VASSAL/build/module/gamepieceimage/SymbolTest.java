package VASSAL.build.module.gamepieceimage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SymbolTest {

  @Test
  public void symbolSizeNamesCannotBeMutatedByCaller() {
    final String[] sizes = Symbol.NatoUnitSymbolSet.getSymbolSizes();
    final String firstSize = sizes[0];

    sizes[0] = "Changed";

    assertEquals(firstSize, Symbol.NatoUnitSymbolSet.getSymbolSizes()[0]);
    assertNotEquals("Changed", Symbol.NatoUnitSymbolSet.getSymbolSizes()[0]);
  }

  @Test
  public void symbolSizeDisplayNamesCannotBeMutatedByCaller() {
    final String[] displayNames = Symbol.NatoUnitSymbolSet.getSymbolSizeDisplayNames();
    final String firstDisplayName = displayNames[0];

    displayNames[0] = "Changed";

    assertEquals(firstDisplayName, Symbol.NatoUnitSymbolSet.getSymbolSizeDisplayNames()[0]);
    assertNotEquals("Changed", Symbol.NatoUnitSymbolSet.getSymbolSizeDisplayNames()[0]);
  }
}
