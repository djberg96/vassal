/*
 *
 * Copyright (c) 2005 by Rodney Kinney, Brent Easton
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License (LGPL) as published by
 * the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, copies are available at
 * http://www.opensource.org.
 */

package VASSAL.build.module.gamepieceimage;

import VASSAL.configure.TranslatableStringEnum;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import javax.swing.KeyStroke;

import VASSAL.i18n.Resources;
import org.apache.commons.lang3.ArrayUtils;

import VASSAL.build.AutoConfigurable;
import VASSAL.configure.Configurer;
import VASSAL.configure.ConfigurerFactory;
import VASSAL.configure.FormattedStringConfigurer;
import VASSAL.configure.VisibilityCondition;
import VASSAL.tools.SequenceEncoder;

public class TextItem extends Item {

  public static final String TYPE = "Text"; //$NON-NLS-1$

  protected static final String FONT = "font"; //$NON-NLS-1$
  protected static final String FONT_FAMILY = "fontFamily"; //$NON-NLS-1$
  protected static final String FONT_SIZE = "fontSize"; //$NON-NLS-1$
  protected static final String FONT_BOLD = "fontBold"; //$NON-NLS-1$
  protected static final String FONT_ITALIC = "fontItalic"; //$NON-NLS-1$
  protected static final String FONT_OUTLINE = "fontOutline"; //$NON-NLS-1$
  protected static final String FONT_OUTLINE_THICKNESS = "fontOutlineThickness"; //$NON-NLS-1$
  protected static final String SOURCE = "source"; //$NON-NLS-1$
  protected static final String TEXT = "text"; //$NON-NLS-1$

  protected static final String LEFT = "left"; //$NON-NLS-1$
  protected static final String CENTER = "center"; //$NON-NLS-1$
  protected static final String RIGHT = "right"; //$NON-NLS-1$
  protected static final String TOP = "top"; //$NON-NLS-1$
  protected static final String BOTTOM = "bottom"; //$NON-NLS-1$

  public static final String SRC_VARIABLE = "Specified in individual images"; // NON-NLS - No really!
  public static final String SRC_FIXED = "Fixed for this layout";  // NON-NLS - No really!

  protected static final String PIECE_NAME = "pieceName"; //$NON-NLS-1$
  protected static final String LABEL = "label"; //$NON-NLS-1$
  protected static final String DEFAULT_FORMAT = "$" + PIECE_NAME + "$"; //$NON-NLS-1$ //$NON-NLS-2$

  public static final int AL_CENTER = 0;
  public static final int AL_RIGHT = 1;
  public static final int AL_LEFT = 2;
  public static final int AL_TOP = 3;
  public static final int AL_BOTTOM = 4;
  private static final int DEFAULT_OUTLINE_THICKNESS = 1;

  protected String fontStyleName = "Default"; //$NON-NLS-1$
  protected String fontFamily = FontManager.DEFAULT;
  protected int fontSize = FontManager.DEFAULT_FONT.getSize();
  protected boolean fontBold = false;
  protected boolean fontItalic = false;
  protected boolean fontOutline = false;
  protected int fontOutlineThickness = DEFAULT_OUTLINE_THICKNESS;
  protected String textSource = SRC_VARIABLE;
  protected String text = ""; //$NON-NLS-1$

  protected String changeCmd = ""; //$NON-NLS-1$
  protected KeyStroke changeKey;
  protected boolean lockable = false;
  protected String lockCmd = ""; //$NON-NLS-1$
  protected KeyStroke lockKey;

  public TextItem() {
    super();
  }

  public TextItem(GamePieceLayout l) {
    super(l);
  }

  public TextItem(GamePieceLayout l, String nam) {
    this(l);
    name = nam;
    localizedName = nam;
  }

  @Override
  public String[] getAttributeDescriptions() {
    return ArrayUtils.insert(
      2, super.getAttributeDescriptions(),
      Resources.getString("Editor.FontConfigurer.font_family"),
      Resources.getString("Editor.FontConfigurer.font_size"),
      Resources.getString("Editor.FontConfigurer.bold_checkbox"),
      Resources.getString("Editor.FontConfigurer.italic_checkbox"),
      Resources.getString("Editor.FontConfigurer.outline_checkbox"),
      Resources.getString("Editor.TextItem.outline_thickness"),
      Resources.getString("Editor.TextItem.text_option"),
      Resources.getString("Editor.TextItem.text")
    );
  }

  @Override
  public Class<?>[] getAttributeTypes() {
    return ArrayUtils.insert(
      2, super.getAttributeTypes(),
      FontFamilyConfig.class,
      Integer.class,
      Boolean.class,
      Boolean.class,
      Boolean.class,
      Integer.class,
      TextSource.class,
      String.class);
  }

  @Override
  public String[] getAttributeNames() {
    return ArrayUtils.insert(
      2, super.getAttributeNames(),
      FONT_FAMILY,
      FONT_SIZE,
      FONT_BOLD,
      FONT_ITALIC,
      FONT_OUTLINE,
      FONT_OUTLINE_THICKNESS,
      SOURCE,
      TEXT);
  }

  public static class FontFamilyConfig implements ConfigurerFactory {
    @Override
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new StringEnumConfigurer(key, name, FontManager.getFontManager().getFontNames());
    }
  }

  @Override
  public void setAttribute(String key, Object o) {
    if (FONT.equals(key)) {
      fontStyleName = (String) o;
      applyFontStyle(FontManager.getFontManager().getFontStyle(fontStyleName));
    }
    else if (FONT_FAMILY.equals(key)) {
      fontFamily = (String) o;
    }
    else if (FONT_SIZE.equals(key)) {
      if (o instanceof String) {
        o = Integer.valueOf((String) o);
      }
      fontSize = Math.max(1, (Integer) o);
    }
    else if (FONT_BOLD.equals(key)) {
      if (o instanceof String) {
        o = Boolean.valueOf((String) o);
      }
      fontBold = Boolean.TRUE.equals(o);
    }
    else if (FONT_ITALIC.equals(key)) {
      if (o instanceof String) {
        o = Boolean.valueOf((String) o);
      }
      fontItalic = Boolean.TRUE.equals(o);
    }
    else if (FONT_OUTLINE.equals(key)) {
      if (o instanceof String) {
        o = Boolean.valueOf((String) o);
      }
      fontOutline = Boolean.TRUE.equals(o);
    }
    else if (FONT_OUTLINE_THICKNESS.equals(key)) {
      if (o instanceof String) {
        o = Integer.valueOf((String) o);
      }
      fontOutlineThickness = Math.max(1, (Integer) o);
    }
    else if (SOURCE.equals(key)) {
      textSource = (String) o;
    }
    else if (TEXT.equals(key)) {
      text = (String) o;
    }
    else {
      super.setAttribute(key, o);
    }

    if (layout != null) {
      layout.refresh();
    }

  }

  @Override
  public String getAttributeValueString(String key) {

    if (FONT.equals(key)) {
      return fontStyleName;
    }
    else if (FONT_FAMILY.equals(key)) {
      return fontFamily;
    }
    else if (FONT_SIZE.equals(key)) {
      return Integer.toString(fontSize);
    }
    else if (FONT_BOLD.equals(key)) {
      return Boolean.toString(fontBold);
    }
    else if (FONT_ITALIC.equals(key)) {
      return Boolean.toString(fontItalic);
    }
    else if (FONT_OUTLINE.equals(key)) {
      return Boolean.toString(fontOutline);
    }
    else if (FONT_OUTLINE_THICKNESS.equals(key)) {
      return Integer.toString(fontOutlineThickness);
    }
    else if (SOURCE.equals(key)) {
      return textSource;
    }
    else if (TEXT.equals(key)) {
      return text;
    }
    else {
      return super.getAttributeValueString(key);
    }
  }

  @Override
  public VisibilityCondition getAttributeVisibility(String name) {
    if (TEXT.equals(name)) {
      return fixedCond;
    }
    else if (FONT_OUTLINE_THICKNESS.equals(name)) {
      return this::isOutline;
    }
    else {
      return super.getAttributeVisibility(name);
    }
  }

  private final VisibilityCondition fixedCond = () -> textSource.equals(SRC_FIXED);

  @Override
  public void draw(Graphics g, GamePieceImage defn) {

    TextItemInstance ti;

    if (defn == null) {
      defn = new GamePieceImage(getLayout());
    }
    ti = defn.getTextInstance(name);

    if (ti == null) {
      ti = new TextItemInstance();
    }

    final Color fg = ti.getFgColor().getColor();
    final Color bg = ti.getBgColor().getColor();
    if (fg == null && bg == null) {
      return;
    }

    final boolean outline = isOutline();
    final Color ol = ti.getOutlineColor().getColor();

    final String compass = GamePieceLayout.getCompassPoint(getLocation());
    int hAlign = AL_CENTER;
    switch (compass.charAt(compass.length() - 1)) {
    case 'W':
      hAlign = AL_LEFT;
      break;
    case 'E':
      hAlign = AL_RIGHT;
    }
    int vAlign = AL_CENTER;
    switch (compass.charAt(0)) {
    case 'N':
      vAlign = AL_TOP;
      break;
    case 'S':
      vAlign = AL_BOTTOM;
    }

    final Point origin = layout.getPosition(this);
    String s = null;
    if (textSource.equals(SRC_FIXED)) {
      s = text;
    }
    else {
      s = ti.getValue();
    }

    final Graphics2D g2d = ((Graphics2D) g);
    final Object aa = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g2d.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        isAntialias() ? RenderingHints.VALUE_ANTIALIAS_ON :
                        RenderingHints.VALUE_ANTIALIAS_OFF
    );

    AffineTransform saveXForm = null;
    if (getRotation() != 0) {
      saveXForm = g2d.getTransform();
      final AffineTransform newXForm =
        AffineTransform.getRotateInstance(Math.toRadians(getRotation()), getLayout().getVisualizerWidth() / 2,
          getLayout().getVisualizerHeight() / 2);
      g2d.transform(newXForm);
    }

    final Font f = getFont();

    drawLabel(g, s, origin.x, origin.y, f, hAlign, vAlign, fg, bg, null, outline, ol, fontOutlineThickness);

    if (saveXForm != null) {
      g2d.setTransform(saveXForm);
    }
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public String getDisplayName() {
    return Resources.getString("Editor.TextItem.component_type");
  }

  @Override
  public Dimension getSize() {
    return new Dimension(0, 0);
  }

  public static Item decode(GamePieceLayout l, String s) {

    final TextItem item = new TextItem(l);
    decode(item, s);
    return item;
  }

  public static void decode(TextItem item, String s) {
    final SequenceEncoder.Decoder sd = new SequenceEncoder.Decoder(s, ';');

    sd.nextToken();
    item.fontStyleName = sd.nextToken(FontManager.DEFAULT);
    if (item.fontStyleName.length() == 0) {
      item.fontStyleName = FontManager.DEFAULT;
    }
    item.applyFontStyle(FontManager.getFontManager().getFontStyle(item.fontStyleName));
    item.textSource = sd.nextToken(SRC_VARIABLE);
    item.text = sd.nextToken(""); //$NON-NLS-1$
    item.changeCmd = sd.nextToken(""); //$NON-NLS-1$
    item.changeKey = sd.nextKeyStroke(null);
    item.lockCmd = sd.nextToken(""); //$NON-NLS-1$
    item.lockKey = sd.nextKeyStroke(null);
    item.lockable = sd.nextBoolean(false);
    item.fontFamily = sd.nextToken(item.fontFamily);
    item.fontSize = sd.nextInt(item.fontSize);
    item.fontBold = sd.nextBoolean(item.fontBold);
    item.fontItalic = sd.nextBoolean(item.fontItalic);
    item.fontOutline = sd.nextBoolean(item.fontOutline);
    item.fontOutlineThickness = sd.nextInt(DEFAULT_OUTLINE_THICKNESS);

  }

  @Override
  public String encode() {

    final SequenceEncoder se1 = new SequenceEncoder(TYPE, ';');

    se1.append(fontStyleName == null ? "" : fontStyleName); //$NON-NLS-1$
    se1.append(textSource);
    se1.append(text);
    se1.append(changeCmd);
    se1.append(changeKey);
    se1.append(lockCmd);
    se1.append(lockKey);
    se1.append(lockable);
    se1.append(fontFamily);
    se1.append(fontSize);
    se1.append(fontBold);
    se1.append(fontItalic);
    se1.append(fontOutline);
    se1.append(fontOutlineThickness);

    final SequenceEncoder se2 = new SequenceEncoder(se1.getValue(), '|');
    se2.append(super.encode());

    return se2.getValue();
  }

  public boolean isOutline() {
    return fontOutline;
  }

  public int getOutlineThickness() {
    return fontOutlineThickness;
  }

  protected OutlineFont getFont() {
    int style = Font.PLAIN;
    if (fontBold) {
      style |= Font.BOLD;
    }
    if (fontItalic) {
      style |= Font.ITALIC;
    }
    final String resolvedFamily = FontManager.getFontManager().getFontStyle(fontFamily).getFont().getName();
    return new OutlineFont(resolvedFamily, style, fontSize, fontOutline);
  }

  private void applyFontStyle(FontStyle style) {
    final OutlineFont font = style.getFont();
    fontFamily = style.getConfigureName();
    fontSize = Math.max(1, font.getSize());
    fontBold = (font.getStyle() & Font.BOLD) != 0;
    fontItalic = (font.getStyle() & Font.ITALIC) != 0;
    fontOutline = font.isOutline();
    fontOutlineThickness = DEFAULT_OUTLINE_THICKNESS;
  }

  public boolean isFixed() {
    return textSource.equals(SRC_FIXED);
  }

  public static class TextSource extends TranslatableStringEnum {
    @Override
    public String[] getValidValues(AutoConfigurable target) {
      return new String[] { SRC_VARIABLE, SRC_FIXED };
    }

    @Override
    public String[] getI18nKeys(AutoConfigurable target) {
      return new String[] {
        "Editor.TextItem.specified_in_individual_images",
        "Editor.TextItem.fixed_for_this_layout"
      };
    }
  }

  public static class NameFormatConfig implements ConfigurerFactory {
    @Override
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new FormattedStringConfigurer(key, name, new String[]{PIECE_NAME, LABEL});
    }
  }

  public static void drawLabel(Graphics g, String text, int x, int y, Font f, int hAlign, int vAlign, Color fgColor, Color bgColor, Color borderColor, boolean outline, Color outlineColor) {
    drawLabel(g, text, x, y, f, hAlign, vAlign, fgColor, bgColor, borderColor, outline, outlineColor, DEFAULT_OUTLINE_THICKNESS);
  }

  public static void drawLabel(Graphics g, String text, int x, int y, Font f, int hAlign, int vAlign, Color fgColor, Color bgColor, Color borderColor, boolean outline, Color outlineColor, int outlineThickness) {
    g.setFont(f);
    final int buffer = g.getFontMetrics().getLeading();
    final int width = g.getFontMetrics().stringWidth(text) + 2 * buffer;
    final int height = g.getFontMetrics().getHeight();
    int x0 = x;
    int y0 = y;
    switch (hAlign) {
    case AL_CENTER:
      x0 = x - width / 2;
      break;
    case AL_RIGHT:
      x0 = x - width;
      break;
    }
    switch (vAlign) {
    case AL_CENTER:
      y0 = y - height / 2;
      break;
    case AL_BOTTOM:
      y0 = y - height;
      break;
    }

    if (bgColor != null) {
      g.setColor(bgColor);
      g.fillRect(x0, y0, width, height);
    }
    if (borderColor != null) {
      g.setColor(borderColor);
      g.drawRect(x0, y0, width, height);
    }

    final int y1 = y0 + g.getFontMetrics().getHeight() - g.getFontMetrics().getDescent();
    final int x1 = x0 + buffer;
    if (outline && outlineColor != null) {
      g.setColor(outlineColor);
      drawOutline(g, text, x1, y1, Math.max(1, outlineThickness));
    }

    g.setColor(fgColor);
    g.drawString(text, x1, y1);

  }

  private static void drawOutline(Graphics g, String text, int x, int y, int thickness) {
    if (thickness == DEFAULT_OUTLINE_THICKNESS) {
      g.drawString(text, x - 1, y - 1);
      g.drawString(text, x - 1, y + 1);
      g.drawString(text, x + 1, y - 1);
      g.drawString(text, x + 1, y + 1);
      return;
    }

    for (int yOffset = -thickness; yOffset <= thickness; ++yOffset) {
      for (int xOffset = -thickness; xOffset <= thickness; ++xOffset) {
        if (xOffset != 0 || yOffset != 0) {
          g.drawString(text, x + xOffset, y + yOffset);
        }
      }
    }
  }
}
