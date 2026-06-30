package VASSAL.build.module.gamepieceimage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import VASSAL.i18n.Resources;
import VASSAL.tools.image.svg.SVGRenderer;

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

    assertEquals("Layout.svg", image.getConfigureName());
    assertEquals("Layout.svg", image.getLocalizedConfigureName());
    assertEquals(layout, image.getLayout());
  }

  @Test
  public void gamePieceImageDefaultsLayoutNameToSvg() {
    assertEquals("counter.svg", GamePieceImage.defaultImageName("counter"));
    assertEquals("counter.svg", GamePieceImage.defaultImageName("counter.svg"));
    assertEquals("counter.png", GamePieceImage.defaultImageName("counter.png"));
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
  public void gamePieceImageCopyPreservesBucketedArchiveName() {
    final GamePieceImage image = new GamePieceImage();
    image.setConfigureName("counter.svg");
    image.setAttribute(GamePieceImage.BUCKET, "Union/Brigade A");

    final GamePieceImage copy = new GamePieceImage(image);

    assertEquals("Union/Brigade A", copy.getAttributeValueString(GamePieceImage.BUCKET));
    assertEquals("Union/Brigade A/counter.svg", copy.getArchiveImageName());
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
  public void textItemDecodeTreatsBlankLegacyFontStyleAsDefault() {
    final TextItem item = new TextItem(new GamePieceLayout());

    TextItem.decode(item, "Text;;Fixed for this layout;Hi;;;;;false");

    assertEquals(FontManager.DEFAULT, item.getAttributeValueString(TextItem.FONT_FAMILY));
    assertEquals(FontManager.DEFAULT_FONT.getSize(), item.getFont().getSize());
  }

  @Test
  public void textBoxItemMigratesLegacyFontStyleToItemFontSettings() {
    final FontManager fontManager = FontManager.getFontManager();
    fontManager.add(new FontStyle(
      "BoxCaption",
      new OutlineFont(FontManager.SANS_SERIF, Font.BOLD, 20, true)
    ));

    final TextBoxItem item = (TextBoxItem) TextBoxItem.decode(
      new GamePieceLayout(),
      "TextBox;40;30;false,Text;BoxCaption;Fixed for this layout;Hi;;;;;false"
    );

    assertEquals("BoxCaption", item.getAttributeValueString(TextItem.FONT_FAMILY));
    assertEquals("20", item.getAttributeValueString(TextItem.FONT_SIZE));
    assertEquals("true", item.getAttributeValueString(TextItem.FONT_BOLD));
    assertEquals("false", item.getAttributeValueString(TextItem.FONT_ITALIC));
    assertEquals("true", item.getAttributeValueString(TextItem.FONT_OUTLINE));
  }

  @Test
  public void fontStylePreservesLegacyFontSettingsWhenSaved() {
    final OutlineFont legacyFont = new OutlineFont(FontManager.SERIF, Font.BOLD | Font.ITALIC, 22, true);
    final FontStyle style = new FontStyle("Caption", legacyFont);

    assertEquals(FontConfigurer.encode(legacyFont), style.getAttributeValueString(FontStyle.STYLE));
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
  public void textItemDecodePreservesLegacyFontSettingsWhenNewFieldsAreMissing() {
    final FontManager fontManager = FontManager.getFontManager();
    fontManager.add(new FontStyle(
      "Caption",
      new OutlineFont(FontManager.SERIF, Font.BOLD, 22, true)
    ));
    final TextItem item = new TextItem(new GamePieceLayout());

    TextItem.decode(item, "Text;Caption;Fixed for this layout;Hi;;;;;false");

    assertEquals("Caption", item.getAttributeValueString(TextItem.FONT_FAMILY));
    assertEquals("22", item.getAttributeValueString(TextItem.FONT_SIZE));
    assertEquals("true", item.getAttributeValueString(TextItem.FONT_BOLD));
    assertEquals("false", item.getAttributeValueString(TextItem.FONT_ITALIC));
    assertEquals("true", item.getAttributeValueString(TextItem.FONT_OUTLINE));
    assertEquals("1", item.getAttributeValueString(TextItem.FONT_OUTLINE_THICKNESS));
    assertEquals("", item.getAttributeValueString(TextItem.FONT_OUTLINE_COLOR));
  }

  @Test
  public void textItemDecodeCoercesMinimumFontSizeAndOutlineThickness() {
    final TextItem item = new TextItem(new GamePieceLayout());

    TextItem.decode(item, "Text;Default;Fixed for this layout;Hi;;;;;false;Default;0;false;false;true;0");

    assertEquals("1", item.getAttributeValueString(TextItem.FONT_SIZE));
    assertEquals("1", item.getAttributeValueString(TextItem.FONT_OUTLINE_THICKNESS));
  }

  @Test
  public void textItemOutlineThicknessDefaultsForLegacyEncoding() {
    final TextItem item = new TextItem(new GamePieceLayout());

    TextItem.decode(item, "Text;Default;Fixed for this layout;Hi;;;;;false");

    assertEquals("1", item.getAttributeValueString(TextItem.FONT_OUTLINE_THICKNESS));
    assertEquals(1, item.getOutlineThickness());
  }

  @Test
  public void textItemOutlineThicknessRoundTripsThroughEncoding() {
    final GamePieceLayout layout = new GamePieceLayout();
    final TextItem item = new TextItem(layout, "Text");

    item.setAttribute(TextItem.FONT_OUTLINE, true);
    item.setAttribute(TextItem.FONT_OUTLINE_THICKNESS, 4);

    final TextItem decoded = assertInstanceOf(TextItem.class, Item.decode(layout, item.encode()));

    assertEquals("4", decoded.getAttributeValueString(TextItem.FONT_OUTLINE_THICKNESS));
    assertEquals(4, decoded.getOutlineThickness());
  }

  @Test
  public void textItemOutlineColorDefaultsForLegacyEncoding() {
    final TextItem item = new TextItem(new GamePieceLayout());

    TextItem.decode(item, "Text;Default;Fixed for this layout;Hi;;;;;false");

    assertEquals("", item.getAttributeValueString(TextItem.FONT_OUTLINE_COLOR));
    assertEquals(Color.RED, item.getOutlineColor().getColor());
  }

  @Test
  public void textItemOutlineColorRoundTripsThroughEncoding() {
    final GamePieceLayout layout = new GamePieceLayout();
    final TextItem item = new TextItem(layout, "Text");

    item.setAttribute(TextItem.FONT_OUTLINE, true);
    item.setAttribute(TextItem.FONT_OUTLINE_COLOR, ColorSwatch.getWhite());

    final TextItem decoded = assertInstanceOf(TextItem.class, Item.decode(layout, item.encode()));

    assertEquals(ColorSwatch.WHITE, decoded.getAttributeValueString(TextItem.FONT_OUTLINE_COLOR));
    assertEquals(Color.WHITE, decoded.getOutlineColor().getColor());
  }

  @Test
  public void textItemOutlineColorTreatsBlankDecodedValueAsUnset() {
    final TextItem item = new TextItem(new GamePieceLayout());

    TextItem.decode(item, "Text;Default;Fixed for this layout;Hi;;;;;false;Default;18;false;false;true;2; ");

    assertEquals("", item.getAttributeValueString(TextItem.FONT_OUTLINE_COLOR));
    assertEquals(Color.RED, item.getOutlineColor().getColor());
  }

  @Test
  public void drawLabelUsesConfigurableOutlineThickness() {
    final int thinPixels = outlinePixelCount(1);
    final int thickPixels = outlinePixelCount(3);

    assertTrue(thickPixels > thinPixels);
  }

  @Test
  public void textItemDrawUsesConfiguredOutlineThickness() {
    final int thinPixels = textItemOutlinePixelCount(1);
    final int thickPixels = textItemOutlinePixelCount(3);

    assertTrue(thickPixels > thinPixels);
  }

  @Test
  public void textItemLayoutPreviewUsesOutlineWithoutInstance() {
    final TextItem item = newOutlinedTextItem(3);
    final BufferedImage image = new BufferedImage(120, 60, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g = image.createGraphics();
    try {
      item.draw(g, null);
    }
    finally {
      g.dispose();
    }

    assertTrue(colorPixelCount(image, Color.RED) > 0);
  }

  @Test
  public void textItemUsesLayoutOutlineColorWhenSet() {
    final TextItem item = newOutlinedTextItem(3);
    item.setAttribute(TextItem.FONT_OUTLINE_COLOR, ColorSwatch.getWhite());
    final BufferedImage image = drawTextItem(item, null);

    assertTrue(colorPixelCount(image, Color.WHITE) > 0);
  }

  @Test
  public void textItemUsesInstanceOutlineColorWhenLayoutColorIsUnset() {
    final TextItem item = newOutlinedTextItem(3);
    final GamePieceLayout layout = item.getLayout();
    final GamePieceImage definition = new GamePieceImage(layout);
    final TextItemInstance instance = new TextItemInstance("Text", TextItem.TYPE, GamePieceLayout.CENTER, "Hi");
    instance.setFgColor(ColorSwatch.getBlack());
    instance.setOutlineColor(ColorSwatch.getWhite());
    instance.addTo(definition);
    definition.getInstances().add(instance);

    final BufferedImage image = drawTextItem(item, definition);

    assertTrue(colorPixelCount(image, Color.WHITE) > 0);
  }

  @Test
  public void getEncodedImageWritesReadablePng() throws IOException {
    final GamePieceImage image = new GamePieceImage();
    final BufferedImage source = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);

    final byte[] encoded = image.getEncodedImage(source);

    assertNotNull(ImageIO.read(new ByteArrayInputStream(encoded)));
  }

  @Test
  public void getEncodedArchiveImageWritesReadableSvgText() throws IOException {
    final GamePieceLayout layout = new GamePieceLayout();
    layout.setWidth(80);
    layout.setHeight(40);

    final TextItem item = new TextItem(layout, "Text");
    item.setAttribute(TextItem.SOURCE, TextItem.SRC_FIXED);
    item.setAttribute(TextItem.TEXT, "Readable");
    layout.addItem(item);

    final GamePieceImage image = new GamePieceImage(layout);

    final byte[] encoded = image.getEncodedArchiveImage("counter.svg");
    final String svg = new String(encoded, StandardCharsets.UTF_8);

    assertTrue(svg.contains("<text"));
    assertTrue(svg.contains("Readable"));
    assertNotNull(new SVGRenderer("counter.svg", new ByteArrayInputStream(encoded)).render());
  }

  @Test
  public void getEncodedArchiveImageWritesTextTypographyToSvg() throws IOException {
    final GamePieceLayout layout = new GamePieceLayout();
    layout.setWidth(100);
    layout.setHeight(60);

    final TextItem item = new TextItem(layout, "Text");
    item.setAttribute(TextItem.SOURCE, TextItem.SRC_FIXED);
    item.setAttribute(TextItem.TEXT, "Styled");
    item.setAttribute(TextItem.FONT_FAMILY, FontManager.SERIF);
    item.setAttribute(TextItem.FONT_SIZE, 18);
    item.setAttribute(TextItem.FONT_BOLD, true);
    item.setAttribute(TextItem.FONT_ITALIC, true);
    item.setAttribute(TextItem.FONT_OUTLINE, true);
    item.setAttribute(TextItem.FONT_OUTLINE_THICKNESS, 2);
    item.setAttribute(TextItem.FONT_OUTLINE_COLOR, ColorSwatch.getWhite());
    layout.addItem(item);

    final GamePieceImage image = new GamePieceImage(layout);
    final byte[] encoded = image.getEncodedArchiveImage("counter.svg");
    final String svg = new String(encoded, StandardCharsets.UTF_8);

    assertTrue(svg.contains("Styled"));
    assertTrue(svg.contains("font-family"));
    assertTrue(svg.contains("font-size"));
    assertTrue(svg.contains("font-weight"));
    assertTrue(svg.contains("font-style"));
    assertTrue(svg.contains("#ffffff") || svg.contains("rgb(255,255,255)"));
    assertNotNull(new SVGRenderer("counter.svg", new ByteArrayInputStream(encoded)).render());
  }

  @Test
  public void getEncodedArchiveImageWritesSvgForCommonItemTypes() throws IOException {
    final GamePieceLayout layout = new GamePieceLayout();
    layout.setWidth(120);
    layout.setHeight(80);
    layout.addItem(new ShapeItem(layout, "Shape"));
    layout.addItem(new SymbolItem(layout, "Symbol"));

    final TextBoxItem textBox = new TextBoxItem(layout, "TextBox");
    textBox.setAttribute(TextItem.SOURCE, TextItem.SRC_FIXED);
    textBox.setAttribute(TextItem.TEXT, "Box");
    layout.addItem(textBox);

    final GamePieceImage image = new GamePieceImage(layout);
    final byte[] encoded = image.getEncodedArchiveImage("counter.svg");

    assertNotNull(new SVGRenderer("counter.svg", new ByteArrayInputStream(encoded)).render());
  }

  @Test
  public void getEncodedArchiveImageWritesSymbolSizeAsVectorSvg() throws IOException {
    final GamePieceLayout layout = new GamePieceLayout();
    layout.setWidth(80);
    layout.setHeight(80);
    layout.addItem(new SymbolItem(layout, "Symbol"));

    final GamePieceImage image = new GamePieceImage(layout);
    final byte[] encoded = image.getEncodedArchiveImage("counter.svg");
    final String svg = new String(encoded, StandardCharsets.UTF_8);

    assertFalse(svg.contains("<image"));
    assertNotNull(new SVGRenderer("counter.svg", new ByteArrayInputStream(encoded)).render());
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

  private static int outlinePixelCount(int thickness) {
    final BufferedImage image = new BufferedImage(120, 60, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g = image.createGraphics();
    try {
      TextItem.drawLabel(
        g,
        "Hi",
        60,
        30,
        new Font(FontManager.SANS_SERIF, Font.PLAIN, 24),
        TextItem.AL_CENTER,
        TextItem.AL_CENTER,
        Color.BLACK,
        null,
        null,
        true,
        Color.RED,
        thickness
      );
    }
    finally {
      g.dispose();
    }

    int count = 0;
    for (int y = 0; y < image.getHeight(); ++y) {
      for (int x = 0; x < image.getWidth(); ++x) {
        if ((image.getRGB(x, y) & 0x00ffffff) == (Color.RED.getRGB() & 0x00ffffff)) {
          ++count;
        }
      }
    }

    return count;
  }

  private static int textItemOutlinePixelCount(int thickness) {
    final TextItem item = newOutlinedTextItem(thickness);
    final GamePieceLayout layout = item.getLayout();

    final GamePieceImage definition = new GamePieceImage(layout);
    final TextItemInstance instance = new TextItemInstance("Text", TextItem.TYPE, GamePieceLayout.CENTER, "Hi");
    instance.setFgColor(ColorSwatch.getBlack());
    instance.setOutlineColor(ColorSwatch.getRed());
    instance.addTo(definition);
    definition.getInstances().add(instance);

    final BufferedImage image = drawTextItem(item, definition);
    return colorPixelCount(image, Color.RED);
  }

  private static TextItem newOutlinedTextItem(int thickness) {
    final GamePieceLayout layout = new GamePieceLayout();
    layout.setWidth(120);
    layout.setHeight(60);

    final TextItem item = new TextItem(layout, "Text");
    item.setAttribute(TextItem.SOURCE, TextItem.SRC_FIXED);
    item.setAttribute(TextItem.TEXT, "Hi");
    item.setAttribute(TextItem.FONT_SIZE, 24);
    item.setAttribute(TextItem.FONT_OUTLINE, true);
    item.setAttribute(TextItem.FONT_OUTLINE_THICKNESS, thickness);
    layout.addItem(item);
    return item;
  }

  private static BufferedImage drawTextItem(TextItem item, GamePieceImage definition) {
    final BufferedImage image = new BufferedImage(120, 60, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g = image.createGraphics();
    try {
      item.draw(g, definition);
    }
    finally {
      g.dispose();
    }

    return image;
  }

  private static int colorPixelCount(BufferedImage image, Color color) {
    int count = 0;
    for (int y = 0; y < image.getHeight(); ++y) {
      for (int x = 0; x < image.getWidth(); ++x) {
        if ((image.getRGB(x, y) & 0x00ffffff) == (color.getRGB() & 0x00ffffff)) {
          ++count;
        }
      }
    }

    return count;
  }
}
