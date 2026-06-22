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

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;

import javax.xml.XMLConstants;

import io.sf.carte.echosvg.anim.dom.SVGDOMImplementation;
import io.sf.carte.echosvg.bridge.BridgeContext;
import io.sf.carte.echosvg.bridge.BridgeException;
import io.sf.carte.echosvg.bridge.DocumentLoader;
import io.sf.carte.echosvg.bridge.UserAgent;
import io.sf.carte.echosvg.ext.awt.image.GraphicsUtil;
import io.sf.carte.echosvg.transcoder.TranscoderException;
import io.sf.carte.echosvg.transcoder.TranscoderInput;
import io.sf.carte.echosvg.transcoder.TranscoderOutput;
import io.sf.carte.echosvg.transcoder.image.ImageTranscoder;

import org.apache.commons.lang3.SystemUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

import VASSAL.build.GameModule;
import VASSAL.tools.DataArchive;
import VASSAL.tools.image.ImageUtils;

/**
 * EchoSVG-backed renderer for SVGs using features unsupported by JSVG.
 */
class EchoSvgRenderer {
  private static final Logger logger =
    LoggerFactory.getLogger(EchoSvgRenderer.class);

  private static final String XLINK_NAMESPACE = "http://www.w3.org/1999/xlink"; //NON-NLS
  private static final String HREF = "href"; //NON-NLS
  private static final String XLINK_HREF = "xlink:href"; //NON-NLS

  private static final double DEGTORAD = Math.PI / 180.0;

  private final SVGDocument doc;
  private final float defaultW, defaultH;
  private final Rasterizer r = new Rasterizer();

  EchoSvgRenderer(String file, InputStream in) throws IOException {
    doc = EchoSvgImageUtils.getDocument(file, in);
    addLegacyXLinkHrefAttributes(doc);

    final Dimension s = EchoSvgImageUtils.getImageSize(doc);
    defaultW = s.width;
    defaultH = s.height;
  }

  private static void addLegacyXLinkHrefAttributes(Document document) {
    final Element root = document.getDocumentElement();
    if (root != null && addLegacyXLinkHrefAttributes(root)) {
      root.setAttributeNS(
        XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:xlink", XLINK_NAMESPACE //NON-NLS
      );
    }
  }

  private static boolean addLegacyXLinkHrefAttributes(Node node) {
    boolean changed = false;

    if (node instanceof Element e) {
      if (e.hasAttribute(HREF) && !e.hasAttributeNS(XLINK_NAMESPACE, HREF)) {
        e.setAttributeNS(XLINK_NAMESPACE, XLINK_HREF, e.getAttribute(HREF));
        changed = true;
      }
    }

    for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
      changed |= addLegacyXLinkHrefAttributes(child);
    }

    return changed;
  }

  public BufferedImage render(double angle, double scale) {
    final AffineTransform px = AffineTransform.getRotateInstance(
      angle * DEGTORAD, defaultW / 2.0, defaultH / 2.0);

    final Rectangle2D rect = new Rectangle2D.Float(0, 0, defaultW, defaultH);
    final Rectangle2D b = px.createTransformedShape(rect).getBounds2D();

    final Document renderDoc = createTransformedDocument(
      AffineTransform.getTranslateInstance(-b.getX(), -b.getY()),
      angle
    );

    r.addTranscodingHint(Rasterizer.KEY_WIDTH, (float) (b.getWidth() * scale));
    r.addTranscodingHint(Rasterizer.KEY_HEIGHT, (float) (b.getHeight() * scale));

    try {
      r.transcode(new TranscoderInput(renderDoc), null);
      return r.getBufferedImage();
    }
    catch (BridgeException | TranscoderException e) {
      logger.error("Failed to render SVG at angle {} and scale {}", angle, scale, e);
      return null;
    }
  }

  public BufferedImage render(double angle, double scale, Rectangle2D aoi) {
    final Document renderDoc = createTransformedDocument(
      AffineTransform.getTranslateInstance(-aoi.getX(), -aoi.getY()),
      angle
    );

    r.addTranscodingHint(Rasterizer.KEY_WIDTH, (float) aoi.getWidth());
    r.addTranscodingHint(Rasterizer.KEY_HEIGHT, (float) aoi.getHeight());

    try {
      r.transcode(new TranscoderInput(renderDoc), null);
      return r.getBufferedImage();
    }
    catch (BridgeException | TranscoderException e) {
      logger.error("Failed to render SVG area {} at angle {} and scale {}", aoi, angle, scale, e);
      return null;
    }
  }

  private Document createTransformedDocument(AffineTransform viewportTransform, double angle) {
    final Document renderDoc = (Document) doc.cloneNode(true);

    final AffineTransform transform = new AffineTransform(viewportTransform);
    transform.rotate(angle * DEGTORAD, defaultW / 2.0, defaultH / 2.0);

    if (SystemUtils.IS_OS_MAC) {
      transform.rotate(0.000001 * DEGTORAD);
    }

    final Element g = renderDoc.createElementNS(
      SVGDOMImplementation.SVG_NAMESPACE_URI, "g" //NON-NLS
    );
    g.setAttributeNS(null, "transform", matrix(transform)); //NON-NLS

    final Element svg = renderDoc.getDocumentElement();
    Node n;
    while ((n = svg.getFirstChild()) != null) {
      g.appendChild(n);
    }

    svg.appendChild(g);
    return renderDoc;
  }

  private static String matrix(AffineTransform transform) {
    final double[] m = new double[6];
    transform.getMatrix(m);
    return "matrix(" + m[0] + ' ' + m[1] + ' ' + m[2] + ' ' +
      m[3] + ' ' + m[4] + ' ' + m[5] + ')'; //NON-NLS
  }

  private static class DataArchiveDocumentLoader extends DocumentLoader {
    public DataArchiveDocumentLoader(UserAgent userAgent) {
      super(userAgent);
    }

    @Override
    public Document loadDocument(String uri)
        throws MalformedURLException, IOException {
      final String file = new File(URI.create(uri).toURL().getPath()).getName();
      final DataArchive mda = GameModule.getGameModule().getDataArchive();
      try (InputStream inner = mda.getInputStream(file);
           BufferedInputStream in = new BufferedInputStream(inner)) {
        return loadDocument(uri, in);
      }
      catch (DOMException e) {
        throw new IOException(e);
      }
    }
  }

  private static class Rasterizer extends ImageTranscoder {
    private final DocumentLoader docLoader;
    private BufferedImage image;

    public Rasterizer() {
      docLoader = new DataArchiveDocumentLoader(getUserAgent());
    }

    @Override
    protected BridgeContext createBridgeContext() {
      return new BridgeContext(getUserAgent(), docLoader);
    }

    @Override
    public BufferedImage createImage(int w, int h) {
      if (w <= 0 || h <= 0) {
        return ImageUtils.NULL_IMAGE;
      }

      final BufferedImage image = ImageUtils.createCompatibleImage(
        w, h, !hints.containsKey(KEY_BACKGROUND_COLOR)
      );

      final Graphics2D g2d = GraphicsUtil.createGraphics(image);
      if (hints.containsKey(KEY_BACKGROUND_COLOR)) {
        final Paint bgcolor = (Paint) hints.get(KEY_BACKGROUND_COLOR);
        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.setPaint(bgcolor);
        g2d.fillRect(0, 0, w, h);
      }
      g2d.dispose();

      return image;
    }

    @Override
    public void writeImage(BufferedImage image, TranscoderOutput output) {
      this.image = image;
    }

    public BufferedImage getBufferedImage() {
      return image;
    }
  }
}
