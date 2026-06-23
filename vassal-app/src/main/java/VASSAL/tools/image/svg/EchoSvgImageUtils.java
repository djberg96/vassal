/*
 *
 * Copyright (c) 2026 by VASSAL Development Team
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
import java.io.IOException;
import java.io.InputStream;

import io.sf.carte.echosvg.anim.dom.SAXSVGDocumentFactory;
import io.sf.carte.echosvg.bridge.BridgeContext;
import io.sf.carte.echosvg.bridge.BridgeException;
import io.sf.carte.echosvg.bridge.UnitProcessor;
import io.sf.carte.echosvg.bridge.UserAgentAdapter;
import io.sf.carte.echosvg.bridge.ViewBox;
import io.sf.carte.echosvg.util.SVGConstants;

import org.apache.commons.lang3.tuple.Pair;

import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

/**
 * EchoSVG-backed subset of {@link SVGImageUtils} used by the compatibility
 * renderer.
 */
final class EchoSvgImageUtils {
  private EchoSvgImageUtils() { }

  // NB: SAXSVGDocumentFactory isn't documented as thread-safe.
  private static final SAXSVGDocumentFactory FACTORY = new SAXSVGDocumentFactory();

  static SVGDocument getDocument(String file, InputStream in) throws IOException {
    try (in) {
      synchronized (FACTORY) {
        return FACTORY.createSVGDocument(file, in);
      }
    }
    catch (DOMException e) {
      throw new IOException(e);
    }
  }

  private static Pair<Float, Boolean> getSVGWidth(SVGSVGElement root, UnitProcessor.Context uctx) throws IOException {
    float w = -1.0f;
    boolean wIsPct = false;
    final String ws = root.getAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE);
    if (!ws.isEmpty()) {
      try {
        w = UnitProcessor.svgHorizontalLengthToUserSpace(
          ws, SVGConstants.SVG_WIDTH_ATTRIBUTE, uctx
        );
      }
      catch (BridgeException e) {
        throw new IOException(e);
      }
      wIsPct = ws.contains("%");
    }

    return Pair.of(w, wIsPct);
  }

  private static Pair<Float, Boolean> getSVGHeight(SVGSVGElement root, UnitProcessor.Context uctx) throws IOException {
    float h = -1.0f;
    boolean hIsPct = false;
    final String hs = root.getAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE);
    if (!hs.isEmpty()) {
      try {
        h = UnitProcessor.svgVerticalLengthToUserSpace(
          hs, SVGConstants.SVG_HEIGHT_ATTRIBUTE, uctx
        );
      }
      catch (BridgeException e) {
        throw new IOException(e);
      }
      hIsPct = hs.contains("%");
    }

    return Pair.of(h, hIsPct);
  }

  private static Pair<float[], Boolean> getViewBox(SVGSVGElement root, BridgeContext bctx) throws IOException {
    float[] vb = null;
    final String vbs = root.getAttributeNS(null, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE);
    if (!vbs.isEmpty()) {
      try {
        vb = ViewBox.parseViewBoxAttribute(root, vbs, bctx);
      }
      catch (BridgeException e) {
        throw new IOException(e);
      }
    }

    return Pair.of(vb, vbs.isEmpty());
  }

  static Dimension getImageSize(SVGDocument doc) throws IOException {
    final SVGSVGElement root = doc.getRootElement();
    try (BridgeContext bctx = new BridgeContext(new UserAgentAdapter())) {
      final UnitProcessor.Context uctx = UnitProcessor.createContext(bctx, root);

      Pair<Float, Boolean> p = getSVGWidth(root, uctx);
      float w = p.getLeft();
      final boolean wIsPct = p.getRight();

      p = getSVGHeight(root, uctx);
      float h = p.getLeft();
      final boolean hIsPct = p.getRight();

      final Pair<float[], Boolean> vbp = getViewBox(root, bctx);
      final float[] vb = vbp.getLeft();
      final boolean vbEmpty = vbp.getRight();

      if (w < 0.0f || h < 0.0f) {
        if (!vbEmpty) {
          if (vb != null) {
            if (w < 0.0f) {
              w = vb[2];
            }
            else if (wIsPct) {
              w *= vb[2];
            }

            if (h < 0.0f) {
              h = vb[3];
            }
            else if (hIsPct) {
              h *= vb[3];
            }
          }
        }
        else {
          if (h >= 0.0f) {
            w = h;
          }
          else if (w >= 0.0f) {
            h = w;
          }
          else {
            w = h = 0;
          }
        }
      }
      else if (!vbEmpty && vb != null) {
        if (wIsPct) {
          w *= vb[2];
        }

        if (hIsPct) {
          h *= vb[3];
        }
      }

      return new Dimension((int) (w + 0.5f), (int) (h + 0.5f));
    }
  }
}
