package VASSAL.build.module.gamepieceimage;

import org.junit.jupiter.api.Test;

import javax.swing.JComboBox;

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

  @Test
  public void sizeConfigurerComboBoxUsesSymbolSizes() {
    final JComboBox<String> comboBox = new SizeConfigurer("size", "Size").getComboBox();

    assertEquals(Symbol.NatoUnitSymbolSet.getSymbolSizes().length, comboBox.getItemCount());
    assertEquals(Symbol.NatoUnitSymbolSet.getSymbolSizes()[0], comboBox.getItemAt(0));
  }

  @Test
  public void symbolConfigurerComboBoxUsesSymbolNames() {
    final JComboBox<String> comboBox = new SymbolConfigurer("symbol", "Symbol").getComboBox();

    assertEquals(Symbol.NatoUnitSymbolSet.getSymbolNames().length, comboBox.getItemCount());
    assertEquals(Symbol.NatoUnitSymbolSet.getSymbolNames()[0], comboBox.getItemAt(0));
  }
}
