/*
 * Copyright 2026 Vassal Development Team
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

package VASSAL.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import javax.swing.Timer;
import org.junit.jupiter.api.Test;

public class DebugControlsTest {
  @Test
  public void constructorCreatesControlsAndSingleTimerListener() throws ReflectiveOperationException {
    final DebugControls controls = new DebugControls();

    assertNotNull(controls.getControls());

    final Field timerField = DebugControls.class.getDeclaredField("timer");
    timerField.setAccessible(true);
    final Timer timer = (Timer) timerField.get(controls);

    assertEquals(1, timer.getActionListeners().length);
  }
}
