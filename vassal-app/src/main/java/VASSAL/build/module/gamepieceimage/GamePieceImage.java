/*
 *
 * Copyright (c) 2005 by Rodney Kinney, Brent Easton
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */

package VASSAL.build.module.gamepieceimage;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.AutoConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.configure.Configurer;
import VASSAL.configure.ConfigurerFactory;
import VASSAL.configure.StringConfigurer;
import VASSAL.configure.VisibilityCondition;
import VASSAL.i18n.Resources;
import VASSAL.tools.ArchiveWriter;
import VASSAL.tools.UniqueIdManager;
import VASSAL.tools.imageop.Op;
import VASSAL.tools.imageop.SourceOp;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

import org.w3c.dom.Element;

/**
 *
 */
public class GamePieceImage extends AbstractConfigurable implements Visualizable, Cloneable, UniqueIdManager.Identifyable {

  protected static final String NAME = "name"; //$NON-NLS-1$
  protected static final String BUCKET = "bucket"; //$NON-NLS-1$
  protected static final String PROPS = "props"; //$NON-NLS-1$

  public static final String PART_SIZE = "Size"; //$NON-NLS-1$
  public static final String PART_SYMBOL1 = "Symbol1"; //$NON-NLS-1$
  public static final String PART_SYMBOL2 = "Symbol2"; //$NON-NLS-1$

  public static final String BG_COLOR = "bgColor"; //$NON-NLS-1$
  public static final String BORDER_COLOR = "borderColor"; //$NON-NLS-1$
  public static final String VERSION = "version";
  public static final String VERSION_0 = "0";
  public static final String VERSION_1 = "1";
  private static final String PNG_SUFFIX = ".png";
  private static final String SVG_SUFFIX = ".svg";

  protected List<ItemInstance> instances = new ArrayList<>();
  protected InstanceConfigurer defnConfig = null;
  protected GamePieceLayout layout;
  protected ColorSwatch bgColor = ColorSwatch.getWhite();
  protected ColorSwatch borderColor = ColorSwatch.getBlack();
  protected String bucket = ""; //$NON-NLS-1$
  protected String id;

  protected static final UniqueIdManager idMgr = new UniqueIdManager("GamePieceImage"); //$NON-NLS-1$
  protected String nameInUse;
  protected Image visImage = null;

  protected SourceOp srcOp;
  /** version number. Existing GPI's loaded from a module will be defaulted to version 0 */
  protected String version = VERSION_0;

  public GamePieceImage() {
    super();
    name = ""; //$NON-NLS-1$
    localizedName = ""; //$NON-NLS-1$
  }

  public GamePieceImage(String s) {
    instances = InstanceConfigurer.StringToProperties(s, null);
  }

  public GamePieceImage(GamePieceLayout l) {
    this();
    name = defaultImageName(l.getConfigureName());
    localizedName = name;
    layout = l;
  }

  public GamePieceImage(GamePieceImage defn) {
    this();
    name = defn.getConfigureName();
    localizedName = name;
    this.layout = defn.getLayout();
    this.bgColor = defn.getBgColor();
    this.borderColor = defn.getBorderColor();
    this.bucket = defn.getBucket();
    this.instances.addAll(defn.getInstances());
  }

  @Override
  public void build(Element e) {
    super.build(e);
    // Newly created GPI, force to version 1
    if (e == null) {
      version = VERSION_1;
    }
  }

  /*
   * The Generic trait needs a deep copy of the Image Definition
   */
  @Override
  public Object clone() {
    return new GamePieceImage(this);
  }

  public List<ItemInstance> getInstances() {
    return instances;
  }

  @Override
  public String[] getAttributeDescriptions() {
    return new String[] {
      Resources.getString("Editor.name_label"),
      Resources.getString("Editor.imageSelector.bucket"),
      Resources.getString("Editor.background_color"),
      Resources.getString("Editor.border_color"),
      "",
      ""
    };
  }

  @Override
  public Class<?>[] getAttributeTypes() {
    return new Class<?>[] {
      ImageNameConfig.class,
      BucketConfig.class,
      BgColorSwatchConfig.class,
      BorderColorSwatchConfig.class,
      DefnConfig.class
    };
  }

  public static class BgColorSwatchConfig implements ConfigurerFactory {
    @Override
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new ColorSwatchConfigurer(key, name, ((GamePieceImage) c).getBgColor());
    }
  }

  public static class BorderColorSwatchConfig implements ConfigurerFactory {
    @Override
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new ColorSwatchConfigurer(key, name, ((GamePieceImage) c).getBorderColor());
    }
  }

  public static class DefnConfig implements ConfigurerFactory {
    static GamePieceImage id;

    @Override
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      id = (GamePieceImage) c;
      id.defnConfig = new InstanceConfigurer(key, name, id);
      id.rebuildInstances();
      return id.defnConfig;
    }

    public static void refresh() {
      if (id.defnConfig != null) {
        id.defnConfig.repack();
      }
    }
  }

  @Override
  public String[] getAttributeNames() {
    return new String[] {NAME, BUCKET, BG_COLOR, BORDER_COLOR, PROPS, VERSION};
  }

  public ColorSwatch getBgColor() {
    return bgColor;
  }

  public ColorSwatch getBorderColor() {
    return borderColor;
  }

  public String getBucket() {
    return bucket;
  }

  public String getVersion() {
    return version;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void setAttribute(String key, Object value) {
    if (NAME.equals(key)) {
      final String oldImageName = getArchiveImageName();
      final String newName = (String) value;
      setConfigureName(newName);
      // If the user manages to type in a proper V1 image name, flip it over to Version 1
      if (! isVersion1() && isVersion1ImageName(newName)) {
        version = VERSION_1;
      }
      moveVisualizerImage(oldImageName);
    }
    else if (BUCKET.equals(key)) {
      final String oldImageName = getArchiveImageName();
      bucket = normalizeBucket((String) value);
      moveVisualizerImage(oldImageName);
    }
    else if (BG_COLOR.equals(key)) {
      if (value instanceof String) {
        value = new ColorSwatch((String) value);
      }
      bgColor = (ColorSwatch) value;
      if (defnConfig != null) {
        defnConfig.visualizer.rebuild();
      }
    }
    else if (BORDER_COLOR.equals(key)) {
      if (value instanceof String) {
        value = new ColorSwatch((String) value);
      }
      borderColor = (ColorSwatch) value;
      if (defnConfig != null) {
        defnConfig.visualizer.rebuild();
      }
    }
    else if (PROPS.equals(key)) {
      if (value instanceof String) {
        value = InstanceConfigurer.StringToProperties((String) value, this);
      }
      if (instances instanceof List<?>) {
        instances = (List<ItemInstance>) value;
      }
      if (defnConfig != null) {
        rebuildInstances();
        defnConfig.visualizer.rebuild();
        defnConfig.repack();
      }
    }
    else if (VERSION.equals(key)) {
      version = (String) value;
    }
    if (defnConfig != null) {
      rebuildVisualizerImage();
    }
  }

  @Override
  public String getAttributeValueString(String key) {
    if (NAME.equals(key)) {
      return getConfigureName();
    }
    else if (BUCKET.equals(key)) {
      return getBucket();
    }
    else if (BG_COLOR.equals(key)) {
      return bgColor.encode();
    }
    else if (BORDER_COLOR.equals(key)) {
      return borderColor.encode();
    }
    else if (PROPS.equals(key)) {
      return InstanceConfigurer.PropertiesToString(instances);
    }
    else if (VERSION.equals(key)) {
      return version;
    }
    else
      return null;
  }

  @Override
  public VisibilityCondition getAttributeVisibility(String name) {
    if (BORDER_COLOR.equals(name)) {
      return borderCond;
    }
    else {
      return super.getAttributeVisibility(name);
    }
  }

  private final VisibilityCondition borderCond = () -> {
    return getLayout() != null && getLayout().isColoredBorder();
  };

  @Override
  public void addLocalImageNames(Collection<String> s) {
    final String imageName = getArchiveImageName();
    if (imageName != null && !imageName.isBlank()) {
      s.add(imageName);
    }
  }

  @Override
  public void removeFrom(Buildable parent) {
    idMgr.remove(this);
  }

  @Override
  public HelpFile getHelpFile() {
    return HelpFile.getReferenceManualPage("GamePieceImage.html"); //$NON-NLS-1$
  }

  @Override
  public Class<?>[] getAllowableConfigureComponents() {
    return new Class<?>[0];
  }

  @Override
  public void addTo(Buildable parent) {
    layout = (GamePieceLayout) parent;
    idMgr.add(this);
    validator = idMgr;
    setAllAttributesUntranslatable();
    rebuildInstances();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  public static String getConfigureTypeName() {
    return Resources.getString("Editor.GamePieceImage.component_type");
  }

  public void refreshConfig() {
    rebuildVisualizerImage();
  }

  public GamePieceLayout getLayout() {
    return layout;
  }

  @Override
  public int getVisualizerHeight() {
    return getLayout().getVisualizerHeight();
  }

  @Override
  public int getVisualizerWidth() {
    return getLayout().getVisualizerWidth();
  }

  @Override
  public Image getVisualizerImage() {
    if (visImage == null) {
      rebuildVisualizerImage();
    }
    return visImage;
  }

  // Build the new image and add to the archive
  @Override
  public void rebuildVisualizerImage() {
    if (layout != null) {
      visImage = layout.buildImage(this);

      final ArchiveWriter w = GameModule.getGameModule().getArchiveWriter();
      if (w != null) {
        final String imageName = getArchiveImageName();
        if (imageName != null && !imageName.isBlank()) {
          w.addImage(imageName,
                     getEncodedArchiveImage(imageName));
          final SourceOp op = Op.load(imageName);
          op.update();
        }
      }
    }
  }

  public byte[] getEncodedImage(BufferedImage bufferedImage) {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      writePng(bufferedImage, out);
    }
    catch (IOException e) {
      throw new IllegalStateException("Unable to encode game piece image as PNG", e); //NON-NLS
    }
    return out.toByteArray();
  }

  byte[] getEncodedArchiveImage(String imageName) {
    if (isSvgName(imageName)) {
      return getEncodedSvgImage();
    }

    return getEncodedImage((BufferedImage) visImage);
  }

  byte[] getEncodedSvgImage() {
    try {
      return layout.buildSvg(this).getBytes(StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      throw new IllegalStateException("Unable to encode game piece image as SVG", e); //NON-NLS
    }
  }

  static void writePng(BufferedImage bufferedImage, OutputStream out) throws IOException {
    if (!ImageIO.write(bufferedImage, "png", out)) { //$NON-NLS-1$
      throw new IOException("No PNG image writer is available"); //NON-NLS
    }
  }

  public ItemInstance getInstance(String name) { //NOPMD
    for (final ItemInstance instance : instances) {
      if (name.equals(instance.getName())) {
        return instance;
      }
    }
    return null;
  }

  public TextItemInstance getTextInstance(String name) {
    for (final ItemInstance instance : instances) {
      if (instance instanceof TextItemInstance) {
        if (name.equals(instance.getName())) {
          return (TextItemInstance) instance;
        }
      }
    }
    return null;
  }

  public TextBoxItemInstance getTextBoxInstance(String name) {
    for (final ItemInstance instance : instances) {
      if (instance instanceof TextBoxItemInstance) {
        if (name.equals(instance.getName())) {
          return (TextBoxItemInstance) instance;
        }
      }
    }
    return null;
  }

  public SymbolItemInstance getSymbolInstance(String name) {
    for (final ItemInstance instance : instances) {
      if (instance instanceof SymbolItemInstance) {
        if (name.equals(instance.getName())) {
          return (SymbolItemInstance) instance;
        }
      }
    }
    return null;
  }

  public ShapeItemInstance getShapeInstance(String name) {
    for (final ItemInstance instance : instances) {
      if (instance instanceof ShapeItemInstance) {
        if (name.equals(instance.getName())) {
          return (ShapeItemInstance) instance;
        }
      }
    }
    return null;
  }

  public ImageItemInstance getImageInstance(String name) {
    for (final ItemInstance instance : instances) {
      if (instance instanceof ImageItemInstance) {
        if (name.equals(instance.getName())) {
          return (ImageItemInstance) instance;
        }
      }
    }
    return null;
  }

  /*
   * Reconcile our current elements with the elements in the owning scheme.
   */
  protected void rebuildInstances() {
    final List<ItemInstance> newInstances = new ArrayList<>();

    if (layout != null) {
      for (final ItemInstance prop : instances) {
        final Item item = layout.getItem(prop.getName());
        if (item != null && item.getType().equals(prop.getType())) {
          prop.setLocation(item.getLocation());
          newInstances.add(prop);
        }
      }

      for (final Item item : layout.getItems()) {
        final String name = item.getConfigureName();
        final String type = item.getType();
        final String location = item.getLocation();

        boolean found = false;
        for (final ItemInstance prop : instances) {
          found = name.equals(prop.getName());
          if (found) break;
        }

        if (!found) {
          final ItemInstance instance =
            ItemInstance.newDefaultInstance(name, type, location);
          instance.addTo(this);
          newInstances.add(instance);
        }
      }
    }

    instances = newInstances;
    if (defnConfig != null) {
      defnConfig.setValue(instances);
    }
    rebuildVisualizerImage();
  }

  public boolean isVersion1() {
    return VERSION_1.equals(version);
  }

  public String getArchiveImageName() {
    return imageNameForBucket(bucket, getConfigureName());
  }

  private void moveVisualizerImage(String oldImageName) {
    final String newImageName = getArchiveImageName();
    if (oldImageName == null || oldImageName.isBlank() || oldImageName.equals(newImageName)) {
      return;
    }
    if (!(visImage instanceof BufferedImage)) {
      return;
    }

    final ArchiveWriter w = GameModule.getGameModule().getArchiveWriter();
    if (w == null) {
      return;
    }

    w.removeImage(oldImageName);
    if (newImageName != null && !newImageName.isBlank()) {
      w.addImage(newImageName, getEncodedArchiveImage(newImageName));
      final SourceOp op = Op.load(newImageName);
      op.update();
    }
  }

  private boolean isVersion1ImageName(String name) {
    return name != null && !name.isEmpty() && name.matches("^[\\w.-]+\\.(png|svg)$");
  }

  static String defaultImageName(String name) {
    if (name == null || name.isBlank() || hasSupportedImageSuffix(name)) {
      return name;
    }

    return name + SVG_SUFFIX;
  }

  private static boolean isSvgName(String name) {
    return name != null && name.toLowerCase(Locale.ROOT).endsWith(SVG_SUFFIX);
  }

  private static boolean hasSupportedImageSuffix(String name) {
    if (name == null) {
      return false;
    }

    final String lowerName = name.toLowerCase(Locale.ROOT);
    return lowerName.endsWith(SVG_SUFFIX) || lowerName.endsWith(PNG_SUFFIX);
  }

  private static int supportedImageSuffixLength(String name) {
    if (name == null) {
      return 0;
    }

    final String lowerName = name.toLowerCase(Locale.ROOT);
    if (lowerName.endsWith(SVG_SUFFIX)) {
      return SVG_SUFFIX.length();
    }
    else if (lowerName.endsWith(PNG_SUFFIX)) {
      return PNG_SUFFIX.length();
    }

    return 0;
  }

  static String imageNameForBucket(String bucket, String fileName) {
    final String normalizedBucket = normalizeBucket(bucket);
    if (fileName == null || fileName.isBlank()) {
      return "";
    }

    return normalizedBucket.isEmpty() ? fileName : normalizedBucket + "/" + fileName;
  }

  static String normalizeBucket(String bucket) {
    if (bucket == null) {
      return "";
    }

    return Arrays.stream(bucket.trim().replace('\\', '/').split("/"))
      .map(String::trim)
      .filter(segment -> !segment.isBlank())
      .filter(segment -> !segment.equals("."))
      .filter(segment -> !segment.equals(".."))
      .reduce((left, right) -> left + "/" + right)
      .orElse("");
  }

  public static class BucketConfig implements ConfigurerFactory {
    @Override
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new StringConfigurer(key, name, ((GamePieceImage) c).getBucket());
    }
  }

  public static class ImageNameConfig implements ConfigurerFactory {
    @Override
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new ImageNameConfigurer(key, name, (GamePieceImage) c);
    }
  }

  static class ImageNameConfigurer extends StringConfigurer {

    private final GamePieceImage gpi;

    public ImageNameConfigurer(String key, String name, GamePieceImage gpi) {
      super(key, name, gpi.getConfigureName());
      this.gpi = gpi;
      setHintKey("Editor.GamePieceImage.png_image_name");
      getControls();
      ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new ImageNameFilter(this));
    }

    public boolean isGpiVersion1() {
      return gpi.isVersion1();
    }

    public void setCaretPosition(int pos) {
      nameField.setCaretPosition(pos);
    }
  }

  /**
   * ImageNameFilter that controls how the user can change the Image Name.
   * If the GPI is still version 0, then no controls. Note we can't force change an image
   * name because other components may reference that image.
   * Once the GPI is version 1, then enforce an image suffix.
   */
  private static class ImageNameFilter extends DocumentFilter {
    private final ImageNameConfigurer config;

    public ImageNameFilter(ImageNameConfigurer config) {
      this.config = config;
    }

    /** Return the current value of the text */
    private String getText(FilterBypass fb) {
      final Document doc = fb.getDocument();
      if (doc == null || doc.getLength() == 0) {
        return "";
      }

      String currentValue;
      try {
        currentValue = doc.getText(0, doc.getLength());
      }
      catch (BadLocationException ignored) {
        currentValue = "";
      }

      return currentValue;
    }

    private boolean hasImageSuffix(FilterBypass fb) {
      return hasSupportedImageSuffix(getText(fb));
    }

    private int suffixStart(FilterBypass fb) {
      return getText(fb).length() - supportedImageSuffixLength(getText(fb));
    }

    /** Ensure the current value ends in a supported image suffix. */
    private void fixImageSuffix(FilterBypass fb, int caretPos) throws BadLocationException {
      if (!hasImageSuffix(fb)) {
        super.replace(fb, getText(fb).length(), 0, SVG_SUFFIX, null);
        config.setCaretPosition(caretPos);
      }
    }

    private String clean(String string) {
      return string == null ? "" : string.replaceAll("[^\\w.-]", "");
    }

    /** Don't let any image suffix at the end of the string be removed and clean unwanted characters */
    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
      if (!config.isGpiVersion1()) {
        super.remove(fb, offset, length);
        return;
      }

      if (hasImageSuffix(fb)) {
        if (length != getText(fb).length() && (offset + length) > suffixStart(fb)) {
          return;
        }
      }
      super.remove(fb, offset, length);
      fixImageSuffix(fb, offset);
    }

    /** Nothing to be inserted within the image suffix at the end and clean unwanted characters */
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
      if (!config.isGpiVersion1()) {
        super.insertString(fb, offset, string, attr);
        return;
      }

      if (hasImageSuffix(fb)) {
        if ((offset) >= suffixStart(fb)) {
          return;
        }
      }
      super.insertString(fb, offset, clean(string), attr);
      fixImageSuffix(fb, offset + clean(string).length());
    }

    /** No part of the image suffix at then end of the current text to be replaced and clean unwanted characters */
    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
      if (!config.isGpiVersion1()) {
        super.replace(fb, offset, length, text, attrs);
        return;
      }

      if (hasImageSuffix(fb)) {
        if (length != getText(fb).length() && (offset + length) > suffixStart(fb)) {
          return;
        }
      }
      super.replace(fb, offset, length, clean(text), attrs);
      fixImageSuffix(fb, length + clean(text).length());
    }
  }
}
