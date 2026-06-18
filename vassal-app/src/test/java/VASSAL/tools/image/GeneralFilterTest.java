/*
 *
 * Copyright (c) 2007-2010 by Joel Uckelman
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

package VASSAL.tools.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

public class GeneralFilterTest {
  @Test
  public void zoomPreservesSourceColorModel() {
    final BufferedImage src =
      new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB_PRE);

    final BufferedImage dst = GeneralFilter.zoom(
      new Rectangle(0, 0, 4, 4),
      src,
      new GeneralFilter.BoxFilter()
    );

    assertEquals(4, dst.getWidth());
    assertEquals(4, dst.getHeight());
    assertSame(src.getColorModel(), dst.getColorModel());
    assertEquals(src.isAlphaPremultiplied(), dst.isAlphaPremultiplied());
  }

  @Test
  public void zoomPreservesOpaqueImageType() {
    final BufferedImage src =
      new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);

    final BufferedImage dst = GeneralFilter.zoom(
      new Rectangle(0, 0, 4, 4),
      src,
      new GeneralFilter.BoxFilter()
    );

    assertEquals(BufferedImage.OPAQUE, dst.getTransparency());
    assertSame(src.getColorModel(), dst.getColorModel());
  }

  /** A program for running filter benchmarks. */
  public static void main(String[] args) throws IOException {
    BufferedImage src = ImageIO.read(new File(args[0]));
    final float scale = Float.parseFloat(args[1]);

    final int dw = (int) (src.getWidth() * scale);
    final int dh = (int) (src.getHeight() * scale);

    int type;
    switch (Integer.parseInt(args[2])) {
    case 0: type = BufferedImage.TYPE_INT_ARGB; break;
    case 1: type = BufferedImage.TYPE_INT_ARGB_PRE; break;
    case 2: type = BufferedImage.TYPE_INT_RGB; break;
    default: throw new IllegalArgumentException();
    }

    final BufferedImage tmp =
      new BufferedImage(src.getWidth(), src.getHeight(), type);

    final Graphics2D g = tmp.createGraphics();
    g.drawImage(src, 0, 0, null);
    g.dispose();

    src = tmp;

    for (long t : run(src, dw, dh, 100)) {
      System.out.println(t);
    }

    System.out.println("Ready...");
    System.in.read();
    System.out.println("Starting...");

    long acc = 0;
    for (long t : run(src, dw, dh, 100)) {
      acc += t;
    }

    System.out.println((double) acc/100);

    System.out.println("Done.");
    System.in.read();
  }

  protected static long[] run(BufferedImage src, int dw, int dh, int times) {
    final GeneralFilter.Filter filter = new GeneralFilter.Lanczos3Filter();

    final long[] time = new long[times];

    for (int i = 0; i < times; ++i) {
      final long start = System.currentTimeMillis();
      final BufferedImage dst = GeneralFilter.zoom(new Rectangle(0, 0, dw, dh), src, filter);
      time[i] = System.currentTimeMillis() - start;

      if (dst.getWidth() != dw || dst.getHeight() != dh) {
        throw new AssertionError("Unexpected filtered image size");
      }
    }

    return time;
  }
}
