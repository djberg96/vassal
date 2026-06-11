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

package VASSAL.build.module.map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import VASSAL.tools.LaunchButton;
import java.awt.Point;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

class MoveCameraButtonTest {
  @Test
  void offsetDestAppliesXAndYOffsetAttributes()
    throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

    try (MockedConstruction<LaunchButton> ignored = Mockito.mockConstruction(LaunchButton.class)) {
      final MoveCameraButton button = new MoveCameraButton();
      button.setAttribute(MoveCameraButton.X_OFFSET, "3");
      button.setAttribute(MoveCameraButton.Y_OFFSET, "5");

      final Point destination = new Point(7, 11);
      final Method offsetDest = MoveCameraButton.class.getDeclaredMethod("offsetDest", Point.class);
      offsetDest.setAccessible(true);
      offsetDest.invoke(button, destination);

      assertEquals(new Point(10, 16), destination);
    }
  }
}
