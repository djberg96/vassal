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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package VASSAL.tools;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class KeyStrokeListenerTest {

  @Test
  public void constructorKeepsInitialKeyStroke() {
    final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK);
    final KeyStrokeListener listener = new KeyStrokeListener(e -> { }, stroke);

    assertEquals(stroke, listener.getKeyStroke());
  }

  @Test
  public void constructorNormalizesZeroKeyCode() {
    final KeyStroke stroke = KeyStroke.getKeyStroke(0, 0);
    final KeyStrokeListener listener = new KeyStrokeListener(e -> { }, stroke);

    assertNull(listener.getKeyStroke());
  }

  @Test
  public void namedConstructorKeepsNamedKeyStroke() {
    final NamedKeyStroke stroke = NamedKeyStroke.of(KeyEvent.VK_F1, InputEvent.CTRL_DOWN_MASK, "Help");
    final NamedKeyStrokeListener listener = new NamedKeyStrokeListener(e -> { }, stroke);

    assertEquals(stroke.getKeyStroke(), listener.getKeyStroke());
    assertSame(stroke, listener.getNamedKeyStroke());
  }
}
