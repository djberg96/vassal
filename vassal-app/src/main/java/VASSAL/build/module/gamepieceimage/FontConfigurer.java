/*
 *
 * Copyright (c) 2000-2003 by Rodney Kinney
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

import VASSAL.configure.BooleanConfigurer;
import VASSAL.configure.Configurer;
import VASSAL.configure.IntConfigurer;
import VASSAL.counters.TraitConfigPanel;
import VASSAL.i18n.Resources;
import VASSAL.tools.SequenceEncoder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;

/**
 * A Configurer for {@link Font}values
 */
public class FontConfigurer extends Configurer {
  static final String PREVIEW_COMPONENT_NAME = "FontConfigurer.preview"; // NON-NLS

  private static final int PREVIEW_COLUMNS = 20;
  private static final int PREVIEW_PADDING = 8;
  private static final Color PREVIEW_OUTLINE_COLOR = Color.RED;

  private TraitConfigPanel p;
  private IntConfigurer size;
  private BooleanConfigurer bold;
  private BooleanConfigurer italic;
  private BooleanConfigurer outline;
  private JComboBox<String> family;
  private FontPreview demo;

  public FontConfigurer(String key, String name) {
    super(key, name);
  }

  public FontConfigurer(String key, String name, OutlineFont f) {
    super(key, name);
    value = f;
  }

  public FontConfigurer(String key, String name, FontStyle f) {
    super(key, name);
    value = f.font;
    this.name = f.getConfigureName();
  }
  @Override
  public String getValueString() {
    return encode((OutlineFont) value);
  }

  @Override
  public void setValue(String s) {
    setValue(decode(s));
  }

  @Override
  public java.awt.Component getControls() {
    if (p == null) {

      // Use a TraitConfig Panel as it works well for a combined 'multi-configurer'
      p = new TraitConfigPanel();

      final JPanel familyPanel = new JPanel(new MigLayout("ins 0")); // NON-NLS
      family = new JComboBox<>();
      //String[] s = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
      for (int i = 0; i < FontManager.ALLOWABLE_FONTS.length; ++i) {
        family.addItem(FontManager.ALLOWABLE_FONTS[i]);
      }
      family.setSelectedItem(value == null ? FontManager.SANS_SERIF : (getFontValue().getFamily()));
      familyPanel.add(family);
      p.add("Editor.FontConfigurer.font_family", familyPanel);

      size = new IntConfigurer(getFontValue().getSize());
      p.add("Editor.FontConfigurer.font_size", size);

      bold = new BooleanConfigurer(isBold());
      p.add("Editor.FontConfigurer.bold_checkbox", bold);
      italic = new BooleanConfigurer(isItalic());
      p.add("Editor.FontConfigurer.italic_checkbox", italic);

      outline = new BooleanConfigurer(isOutline());
      p.add("Editor.FontConfigurer.outline_checkbox", outline);

      final JPanel demoPanel = new JPanel(new MigLayout("ins 0", "grow,fill")); // NON-NLS
      demo = new FontPreview(Resources.getString("Editor.FontConfigurer.sample_text"));
      demoPanel.add(demo, "grow"); // NON-NLS
      p.add("Editor.FontConfigurer.sample_label", demoPanel, "grow"); // NON-NLS

      updateValue();

      final ItemListener l = evt -> updateValue();
      family.addItemListener(l);

      final PropertyChangeListener pc = evt -> updateValue();
      size.addPropertyChangeListener(pc);
      bold.addPropertyChangeListener(pc);
      italic.addPropertyChangeListener(pc);
      outline.addPropertyChangeListener(pc);
    }
    return p;
  }

  protected void updateValue() {
    final int style = Font.PLAIN |
       (bold.booleanValue() ? Font.BOLD : 0) |
       (italic.booleanValue() ? Font.ITALIC : 0);

    final OutlineFont font = new OutlineFont(
      (String) family.getSelectedItem(),
      style,
      Integer.parseInt(size.getValueString()),
      outline.booleanValue()
    );

    setValue(font);
    demo.setPreviewFont(font);

    final Window w = SwingUtilities.getWindowAncestor(getControls());
    if (w != null) {
      w.pack();
    }
  }

  protected OutlineFont getFontValue() {
    return (OutlineFont) getValue();
  }

  public static OutlineFont decode(String s) {
    final SequenceEncoder.Decoder sd = new SequenceEncoder.Decoder(s, ',');
    return new OutlineFont(
        sd.nextToken(FontManager.DIALOG),
        sd.nextInt(Font.PLAIN),
        sd.nextInt(10),
        sd.nextBoolean(false));
  }

  public static String encode(OutlineFont f) {
    final SequenceEncoder se = new SequenceEncoder(f.getName(), ',');
    se.append(f.getStyle());
    se.append(f.getSize());
    se.append(f.isOutline());
    return se.getValue();
  }

  public boolean isBold() {
    return (getFontValue().getStyle() & Font.BOLD) != 0;
  }

  public boolean isItalic() {
    return (getFontValue().getStyle() & Font.ITALIC) != 0;
  }

  public boolean isOutline() {
    return getFontValue().isOutline();
  }

  private static class FontPreview extends JComponent {
    private static final long serialVersionUID = 1L;

    private final String text;
    private OutlineFont previewFont;

    FontPreview(String text) {
      this.text = text;
      setName(PREVIEW_COMPONENT_NAME);
      setOpaque(true);
      setBackground(Color.WHITE);
      setForeground(Color.BLACK);
      setBorder(BorderFactory.createEtchedBorder());
    }

    void setPreviewFont(OutlineFont font) {
      previewFont = font;
      setFont(font);
      revalidate();
      repaint();
    }

    @Override
    public Dimension getPreferredSize() {
      final Font font = getFont();
      final FontMetrics metrics = getFontMetrics(font);
      return new Dimension(
        metrics.charWidth('m') * PREVIEW_COLUMNS + PREVIEW_PADDING * 2,
        metrics.getHeight() + PREVIEW_PADDING * 2
      );
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      if (previewFont == null) {
        return;
      }

      TextItem.drawLabel(
        g,
        text,
        getWidth() / 2,
        getHeight() / 2,
        previewFont,
        TextItem.AL_CENTER,
        TextItem.AL_CENTER,
        getForeground(),
        null,
        null,
        previewFont.isOutline(),
        PREVIEW_OUTLINE_COLOR
      );
    }
  }
}
