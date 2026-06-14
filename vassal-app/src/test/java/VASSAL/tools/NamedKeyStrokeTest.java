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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

import org.junit.jupiter.api.Test;

public class NamedKeyStrokeTest {

  @Test
  public void matchesEquivalentRawKeyStroke() {
    final KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK);
    final NamedKeyStroke namedKeyStroke = NamedKeyStroke.of(keyStroke);

    assertTrue(namedKeyStroke.matches(keyStroke));
    assertTrue(namedKeyStroke.equals(keyStroke));
  }

  @Test
  public void doesNotMatchDifferentModifiers() {
    final NamedKeyStroke namedKeyStroke = NamedKeyStroke.of(
      KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK)
    );
    final KeyStroke shifted = KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.SHIFT_DOWN_MASK);

    assertFalse(namedKeyStroke.matches(shifted));
  }

  @Test
  public void treatsDeleteAndBackspaceAsEquivalentWhenModifiersMatch() {
    final NamedKeyStroke delete = NamedKeyStroke.of(
      KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_DOWN_MASK)
    );
    final KeyStroke backspace = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.CTRL_DOWN_MASK);
    final KeyStroke shiftedBackspace = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.SHIFT_DOWN_MASK);

    assertTrue(delete.matches(backspace));
    assertFalse(delete.matches(shiftedBackspace));
  }

  @Test
  public void nullKeyStrokeDoesNotMatchRawKeyStroke() {
    final KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_A, 0);

    assertFalse(NamedKeyStroke.NULL_KEYSTROKE.matches(keyStroke));
    assertFalse(NamedKeyStroke.NULL_KEYSTROKE.matches(null));
  }
}
