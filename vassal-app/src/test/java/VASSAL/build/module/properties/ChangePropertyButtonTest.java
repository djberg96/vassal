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

package VASSAL.build.module.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import VASSAL.build.GameModule;
import VASSAL.tools.KeyStrokeListener;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class ChangePropertyButtonTest {
  @Test
  public void attributesRoundTripThroughConfigurer() {
    final GameModule gameModule = mock(GameModule.class);
    try (MockedStatic<GameModule> staticGameModule = Mockito.mockStatic(GameModule.class)) {
      staticGameModule.when(GameModule::getGameModule).thenReturn(gameModule);

      final ChangePropertyButton button = new ChangePropertyButton();

      button.setAttribute(ChangePropertyButton.DESCRIPTION, "Track initiative");
      button.setAttribute(ChangePropertyButton.REPORT_FORMAT, "$oldValue$ -> $newValue$");
      button.setAttribute(ChangePropertyButton.PROPERTY_CHANGER, "P,new value");

      assertEquals("Track initiative", button.getAttributeValueString(ChangePropertyButton.DESCRIPTION));
      assertEquals("$oldValue$ -> $newValue$", button.getAttributeValueString(ChangePropertyButton.REPORT_FORMAT));
      assertEquals("P,new value", button.getAttributeValueString(ChangePropertyButton.PROPERTY_CHANGER));
      assertNotNull(button.getPropertyChanger());

      Mockito.verify(gameModule).addKeyStrokeListener(any(KeyStrokeListener.class));
    }
  }
}
