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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
