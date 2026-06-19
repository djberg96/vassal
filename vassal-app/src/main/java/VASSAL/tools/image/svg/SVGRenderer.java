/*
 *
 * Copyright (c) 2007-2008 by Joel Uckelman
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

package VASSAL.tools.image.svg;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.parser.resources.ResourcePolicy;
import com.github.weisj.jsvg.view.ViewBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import VASSAL.tools.image.ImageUtils;

/**
 * Render an SVG image to a {@link BufferedImage}.
 *
 * @author Joel Uckelman
 * @since 3.1.0
 */
public class SVGRenderer {
  private static final Logger logger =
    LoggerFactory.getLogger(SVGRenderer.class);

  private static final LoaderContext LOADER_CONTEXT = LoaderContext.builder()
    .externalResourcePolicy(ResourcePolicy.ALLOW_RELATIVE)
    .build();

  private static final double DEGTORAD = Math.PI / 180.0;

  private final SVGDocument doc;
  private final float defaultW, defaultH;

  /**
   * Closes the {@link InputStream}.
   */
  public SVGRenderer(URL file, InputStream in) throws IOException {
    this(toURI(file), in);
  }

  /**
   * Closes the {@link InputStream}.
   */
  public SVGRenderer(String file, InputStream in) throws IOException {
    this(toURI(file), in);
  }

  private SVGRenderer(URI file, InputStream in) throws IOException {
    final byte[] svg;
    try (in) {
      svg = in.readAllBytes();
    }

    doc = new SVGLoader().load(new ByteArrayInputStream(svg), file, LOADER_CONTEXT);
    if (doc == null) {
      throw new IOException("Could not load SVG " + file);
    }

    final Dimension size = SVGImageUtils.getImageSize(
      file.toString(), new ByteArrayInputStream(svg)
    );
    defaultW = size.width;
    defaultH = size.height;
  }

  private static URI toURI(URL file) throws IOException {
    try {
      return file.toURI();
    }
    catch (URISyntaxException e) {
      throw new IOException(e);
    }
  }

  private static URI toURI(String file) {
    try {
      return URI.create(file);
    }
    catch (IllegalArgumentException e) {
      return new File(file).toURI();
    }
  }

  public BufferedImage render() {
    return render(0.0, 1.0);
  }

  public BufferedImage render(double angle, double scale) {
    final AffineTransform px = AffineTransform.getRotateInstance(
      angle * DEGTORAD, defaultW / 2.0, defaultH / 2.0);
    px.scale(scale, scale);

    final Rectangle2D rect = new Rectangle2D.Float(0, 0, defaultW, defaultH);
    final Rectangle2D bounds = px.createTransformedShape(rect).getBounds2D();

    final int w = Math.max(1, (int) (bounds.getWidth() + 0.5));
    final int h = Math.max(1, (int) (bounds.getHeight() + 0.5));

    try {
      final BufferedImage image = ImageUtils.createCompatibleTranslucentImage(w, h);
      final Graphics2D g = image.createGraphics();
      setQualityRenderingHints(g);
      g.translate(-bounds.getX(), -bounds.getY());
      g.rotate(angle * DEGTORAD, defaultW / 2.0, defaultH / 2.0);
      g.scale(scale, scale);
      doc.render(null, g, new ViewBox(defaultW, defaultH));
      g.dispose();
      return image;
    }
    catch (RuntimeException e) {
      logger.error("Failed to render SVG at angle {} and scale {}", angle, scale, e);
      return null;
    }
  }

  public BufferedImage render(double angle, double scale, Rectangle2D aoi) {
    final int w = Math.max(1, (int) (aoi.getWidth() + 0.5));
    final int h = Math.max(1, (int) (aoi.getHeight() + 0.5));

    try {
      final BufferedImage image = ImageUtils.createCompatibleTranslucentImage(w, h);
      final Graphics2D g = image.createGraphics();
      setQualityRenderingHints(g);
      g.translate(-aoi.getX(), -aoi.getY());
      g.rotate(angle * DEGTORAD, defaultW / 2.0, defaultH / 2.0);
      g.scale(scale, scale);
      doc.render(null, g, new ViewBox(defaultW, defaultH));
      g.dispose();
      return image;
    }
    catch (RuntimeException e) {
      logger.error("Failed to render SVG area {} at angle {} and scale {}", aoi, angle, scale, e);
      return null;
    }
  }

  private static void setQualityRenderingHints(Graphics2D g) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
  }
}
