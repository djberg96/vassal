package VASSAL.build.module.gamepieceimage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Font;
import java.util.List;

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

  @Test
  public void gamePieceImageUsesLayoutName() {
    final GamePieceLayout layout = new GamePieceLayout();
    layout.setConfigureName("Layout");
    final GamePieceImage image = new GamePieceImage(layout);

    assertEquals("Layout", image.getConfigureName());
    assertEquals("Layout", image.getLocalizedConfigureName());
    assertEquals(layout, image.getLayout());
  }

  @Test
  public void gamePieceImageParsesEncodedInstances() {
    final List<ItemInstance> items = List.of(
      new TextItemInstance("Text", TextItem.TYPE, GamePieceLayout.N, "Hi"),
      new ImageItemInstance("Image", ImageItem.TYPE, GamePieceLayout.CENTER, "counter.png")
    );
    final String encoded = InstanceConfigurer.PropertiesToString(items);

    final GamePieceImage image = new GamePieceImage(encoded);

    assertEquals(encoded, image.getAttributeValueString(GamePieceImage.PROPS));
  }
}
