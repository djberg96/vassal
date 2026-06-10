package VASSAL.build.module.gamepieceimage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Font;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GamePieceImageConfigurerTest {

  @BeforeEach
  public void setupColorManager() {
    new ColorManager().addTo(null);
  }

  @Test
  public void fontConfigurerKeepsConstructorFontValue() {
    final OutlineFont font = new OutlineFont(FontManager.SANS_SERIF, Font.BOLD, 18, true);
    final FontConfigurer configurer = new FontConfigurer("font", "Font", font);

    assertEquals(font, configurer.getValue());
    assertEquals(FontConfigurer.encode(font), configurer.getValueString());
  }

  @Test
  public void swatchComboBoxUsesColorManagerNames() {
    final String[] colorNames = ColorManager.getColorManager().getColorNames();
    final SwatchComboBox comboBox = new SwatchComboBox();

    assertEquals(colorNames.length, comboBox.getItemCount());
    assertEquals(colorNames[0], comboBox.getItemAt(0));
  }
}
