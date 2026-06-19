/*
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
package VASSAL.configure;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class ImageSelectorTest {
  @Test
  public void bucketsComeFromImagePaths() {
    assertArrayEquals(
      new String[] {
        ImageSelector.ALL_BUCKETS,
        ImageSelector.NO_BUCKET,
        "Confederate/BrigadeA",
        "Union",
        "Union/BrigadeA"
      },
      ImageSelector.bucketsFor(new String[] {
        "flat.png",
        "Union/flag.png",
        "Union/BrigadeA/seventh_infantry.png",
        "Confederate/BrigadeA/seventh_infantry.png"
      })
    );
  }

  @Test
  public void imagesCanBeFilteredByBucket() {
    final ImageSelector.ImageChoice[] choices = ImageSelector.imageChoicesForBucket(
      new String[] {
        "flat.png",
        "Union/flag.png",
        "Union/BrigadeA/seventh_infantry.png",
        "Confederate/BrigadeA/seventh_infantry.png"
      },
      "Union/BrigadeA"
    );

    assertNull(choices[0].imageName());
    assertEquals("Union/BrigadeA/seventh_infantry.png", choices[1].imageName());
    assertEquals("seventh_infantry.png", choices[1].toString());
  }

  @Test
  public void bucketNamesAreNormalizedBeforeImport() {
    assertEquals(
      "Union/BrigadeA/seventh_infantry.png",
      ImageSelector.imageNameForBucket(" /Union//./BrigadeA/../ ", "seventh_infantry.png")
    );
    assertEquals("flat.png", ImageSelector.imageNameForBucket(ImageSelector.NO_BUCKET, "flat.png"));
  }
}
