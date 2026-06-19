package VASSAL.build.module.gamepieceimage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import VASSAL.i18n.Resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GamePieceImageConfigurerTest {

  @BeforeEach
  public void setupColorManager() {
    new ColorManager().addTo(null);
    final FontManager fontManager = new FontManager();
    fontManager.addTo(null);
    fontManager.build(null);
  }

  @Test
  public void fontConfigurerKeepsConstructorFontValue() {
    final OutlineFont font = new OutlineFont(FontManager.SANS_SERIF, Font.BOLD, 18, true);
    final FontConfigurer configurer = new FontConfigurer("font", "Font", font);

    assertEquals(font, configurer.getValue());
    assertEquals(FontConfigurer.encode(font), configurer.getValueString());
  }

  @Test
  public void fontConfigurerExposesOutlineControl() {
    final OutlineFont font = new OutlineFont(FontManager.SANS_SERIF, Font.PLAIN, 18, false);
    final FontConfigurer configurer = new FontConfigurer("font", "Font", font);
    final Component controls = configurer.getControls();

    final JCheckBox outlineBox = findLabeledCheckBox(
      controls,
      Resources.getString("Editor.FontConfigurer.outline_checkbox")
    );

    outlineBox.doClick();

    assertEquals(true, FontConfigurer.decode(configurer.getValueString()).isOutline());
    assertEquals(
      true,
      ((OutlineFont) findNamedComponent(
        controls,
        FontConfigurer.PREVIEW_COMPONENT_NAME
      ).getFont()).isOutline()
    );
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
  public void gamePieceImageStoresArchiveNameInBucket() {
    final GamePieceImage image = new GamePieceImage();
    image.setConfigureName("counter.png");
    image.setAttribute(GamePieceImage.BUCKET, " Union / Brigade A ");

    assertEquals("Union/Brigade A", image.getAttributeValueString(GamePieceImage.BUCKET));
    assertEquals("Union/Brigade A/counter.png", image.getArchiveImageName());
  }

  @Test
  public void gamePieceImageNormalizesUnsafeBucketSegments() {
    assertEquals(
      "Union/Brigade A",
      GamePieceImage.normalizeBucket(" /Union/./../Brigade A/ ")
    );
    assertEquals(
      "Union/Brigade A/counter.png",
      GamePieceImage.imageNameForBucket("Union\\Brigade A", "counter.png")
    );
  }

  @Test
  public void gamePieceImageReportsBucketedLocalImageName() {
    final GamePieceImage image = new GamePieceImage();
    image.setConfigureName("counter.png");
    image.setAttribute(GamePieceImage.BUCKET, "Union");
    final List<String> imageNames = new ArrayList<>();

    image.addLocalImageNames(imageNames);

    assertEquals(List.of("Union/counter.png"), imageNames);
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

  @Test
  public void textItemDrawUsesFallbackDefinitionForVariableText() {
    final GamePieceLayout layout = new GamePieceLayout();
    final TextItem item = new TextItem(layout, "Text");
    final BufferedImage image = new BufferedImage(80, 80, BufferedImage.TYPE_INT_ARGB);

    final Graphics2D g = image.createGraphics();
    try {
      item.draw(g, null);
    }
    finally {
      g.dispose();
    }

    assertTrue(hasVisiblePixel(image));
  }

  @Test
  public void textItemMigratesLegacyFontStyleToItemFontSettings() {
    final FontManager fontManager = FontManager.getFontManager();
    fontManager.add(new FontStyle(
      "Caption",
      new OutlineFont(FontManager.SERIF, Font.BOLD | Font.ITALIC, 22, true)
    ));

    final TextItem item = new TextItem(new GamePieceLayout());
    TextItem.decode(item, "Text;Caption;Fixed for this layout;Hi;;;;;false");

    assertEquals("Caption", item.getAttributeValueString(TextItem.FONT_FAMILY));
    assertEquals("22", item.getAttributeValueString(TextItem.FONT_SIZE));
    assertEquals("true", item.getAttributeValueString(TextItem.FONT_BOLD));
    assertEquals("true", item.getAttributeValueString(TextItem.FONT_ITALIC));
    assertEquals("true", item.getAttributeValueString(TextItem.FONT_OUTLINE));
  }

  @Test
  public void textItemFontSettingsRoundTripThroughEncoding() {
    FontManager.getFontManager().add(new FontStyle(
      "Mono",
      new OutlineFont(FontManager.MONOSPACED, Font.PLAIN, 12, false)
    ));
    final GamePieceLayout layout = new GamePieceLayout();
    final TextItem item = new TextItem(layout, "Text");

    item.setAttribute(TextItem.FONT_FAMILY, "Mono");
    item.setAttribute(TextItem.FONT_SIZE, 18);
    item.setAttribute(TextItem.FONT_BOLD, true);
    item.setAttribute(TextItem.FONT_ITALIC, false);
    item.setAttribute(TextItem.FONT_OUTLINE, true);

    final Item decoded = Item.decode(layout, item.encode());

    final TextItem decodedText = assertInstanceOf(TextItem.class, decoded);
    assertEquals("Mono", decoded.getAttributeValueString(TextItem.FONT_FAMILY));
    assertEquals("18", decoded.getAttributeValueString(TextItem.FONT_SIZE));
    assertEquals("true", decoded.getAttributeValueString(TextItem.FONT_BOLD));
    assertEquals("false", decoded.getAttributeValueString(TextItem.FONT_ITALIC));
    assertEquals("true", decoded.getAttributeValueString(TextItem.FONT_OUTLINE));
    assertEquals(FontManager.MONOSPACED, decodedText.getFont().getName());
  }

  @Test
  public void getEncodedImageWritesReadablePng() throws IOException {
    final GamePieceImage image = new GamePieceImage();
    final BufferedImage source = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);

    final byte[] encoded = image.getEncodedImage(source);

    assertNotNull(ImageIO.read(new ByteArrayInputStream(encoded)));
  }

  @Test
  public void writePngReportsOutputFailure() {
    final BufferedImage source = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);

    assertThrows(
      IOException.class,
      () -> GamePieceImage.writePng(source, new FailingOutputStream())
    );
  }

  private static class FailingOutputStream extends OutputStream {
    @Override
    public void write(int b) throws IOException {
      throw new IOException("write failed");
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      throw new IOException("write failed");
    }
  }

  private static JCheckBox findLabeledCheckBox(Component root, String labelText) {
    if (!(root instanceof Container)) {
      throw new AssertionError("No container to search for " + labelText);
    }

    final Component[] components = ((Container) root).getComponents();
    for (int i = 0; i < components.length - 1; ++i) {
      if (components[i] instanceof JLabel
        && labelText.equals(((JLabel) components[i]).getText())) {
        return findCheckBox(components[i + 1]);
      }
    }

    for (final Component component : components) {
      if (component instanceof Container) {
        try {
          return findLabeledCheckBox(component, labelText);
        }
        catch (AssertionError e) {
          // Try the next nested container.
        }
      }
    }

    throw new AssertionError("No checkbox labeled " + labelText);
  }

  private static JCheckBox findCheckBox(Component root) {
    if (root instanceof JCheckBox) {
      return (JCheckBox) root;
    }

    if (root instanceof Container) {
      for (final Component component : ((Container) root).getComponents()) {
        try {
          return findCheckBox(component);
        }
        catch (AssertionError e) {
          // Try the next nested component.
        }
      }
    }

    throw new AssertionError("No checkbox found");
  }

  private static Component findNamedComponent(Component root, String name) {
    if (name.equals(root.getName())) {
      return root;
    }

    if (root instanceof Container) {
      for (final Component component : ((Container) root).getComponents()) {
        try {
          return findNamedComponent(component, name);
        }
        catch (AssertionError e) {
          // Try the next nested component.
        }
      }
    }

    throw new AssertionError("No component named " + name);
  }

  private static boolean hasVisiblePixel(BufferedImage image) {
    for (int y = 0; y < image.getHeight(); ++y) {
      for (int x = 0; x < image.getWidth(); ++x) {
        if ((image.getRGB(x, y) >>> 24) != 0) {
          return true;
        }
      }
    }

    return false;
  }
}
