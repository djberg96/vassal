package VASSAL.build.module.gamepieceimage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
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
}
