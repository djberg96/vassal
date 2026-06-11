/*
 * Copyright 2026 Vassal Development Team
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available at
 * http://www.opensource.org.
 */

package VASSAL.build.module;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ObscurableOptionsTest {
  @BeforeEach
  @AfterEach
  void clearGlobalOptions() {
    ObscurableOptions.getInstance().setup(false);
  }

  @Test
  void setAllowedSnapshotsAllowedIds() {
    final List<String> allowedIds = new ArrayList<>(List.of("alice"));
    final ObscurableOptions.SetAllowed command = new ObscurableOptions.SetAllowed(allowedIds);

    allowedIds.clear();
    allowedIds.add("bob");

    command.execute();

    assertTrue(ObscurableOptions.getInstance().isUnmaskable("alice"));
    assertFalse(ObscurableOptions.getInstance().isUnmaskable("bob"));
  }

  @Test
  void setAllowedIdsAreReadOnly() {
    final ObscurableOptions.SetAllowed command = new ObscurableOptions.SetAllowed(List.of("alice"));

    assertThrows(UnsupportedOperationException.class, () -> command.getAllowedIds().add("bob"));
  }
}
