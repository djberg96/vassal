/*
 *
 * Copyright (c) 2026 by The VASSAL Development Team
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

import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageUtilsTest {

  @Test
  public void imageSuffixesAreImmutable() {
    assertThrows(UnsupportedOperationException.class, () -> ImageUtils.IMAGE_SUFFIXES.add(".changed"));
  }

  @Test
  public void imageSuffixArrayAccessorReturnsCopy() {
    final String[] suffixes = ImageUtils.imageSuffixes();
    suffixes[0] = ".changed";

    assertEquals(ImageUtils.GIF_SUFFIX, ImageUtils.imageSuffixes()[0]);
  }

  @Test
  public void recognizesKnownImageSuffixesCaseInsensitively() {
    assertTrue(ImageUtils.hasImageSuffix("counter.PNG"));
    assertTrue(ImageUtils.hasImageSuffix("counter.jpeg"));
    assertFalse(ImageUtils.hasImageSuffix("counter.txt"));
  }

  @Test
  public void stripsKnownImageSuffixesCaseInsensitively() {
    assertEquals("counter", ImageUtils.stripImageSuffix("counter.SVG"));
    assertEquals("counter", ImageUtils.stripImageSuffix("counter.jpg"));
    assertEquals("counter.txt", ImageUtils.stripImageSuffix("counter.txt"));
  }

  @Test
  public void transformReturnsSourceForIdentityTransform() {
    final BufferedImage src = new BufferedImage(2, 3, BufferedImage.TYPE_INT_ARGB);

    assertSame(src, ImageUtils.transform(src, 1.0, 0.0));
  }

  @Test
  public void transformReturnsNullImageWhenScaleRemovesImage() {
    final BufferedImage src = new BufferedImage(2, 3, BufferedImage.TYPE_INT_ARGB);

    assertSame(ImageUtils.NULL_IMAGE, ImageUtils.transform(src, 0.0, 0.0));
  }

  @Test
  public void positiveImageRotationUsesHistoricalDirection() {
    final BufferedImage src = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
    final int red = 0xFFFF0000;
    final int blue = 0xFF0000FF;

    src.setRGB(1, 0, red);
    src.setRGB(1, 2, blue);

    final BufferedImage rotated = ImageUtils.transform(src, 1.0, 90.0);

    assertEquals(3, rotated.getWidth());
    assertEquals(3, rotated.getHeight());
    assertEquals(red, rotated.getRGB(0, 1));
    assertEquals(blue, rotated.getRGB(2, 1));
  }

  @Test
  public void rectangleTransformUsesHistoricalAngleConvention() {
    final Rectangle transformed =
      ImageUtils.transform(new Rectangle(-1, -1, 2, 3), 1.0, 90.0);

    assertEquals(new Rectangle(-2, -1, 4, 3), transformed);
  }
}
