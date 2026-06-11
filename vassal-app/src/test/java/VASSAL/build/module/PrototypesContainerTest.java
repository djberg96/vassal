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

package VASSAL.build.module;

import static org.junit.jupiter.api.Assertions.assertNull;

import VASSAL.build.GameModule;
import java.lang.reflect.Field;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class PrototypesContainerTest {
  private Field instanceField;

  @BeforeEach
  public void clearInstance() throws ReflectiveOperationException {
    instanceField = PrototypesContainer.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
    instanceField.set(null, null);
  }

  @AfterEach
  public void restoreInstanceAccess() {
    instanceField.setAccessible(false);
  }

  @Test
  public void getPrototypeWithoutGameModuleReturnsNull() {
    try (MockedStatic<GameModule> gameModule = Mockito.mockStatic(GameModule.class)) {
      gameModule.when(GameModule::getGameModule).thenReturn(null);

      assertNull(PrototypesContainer.getPrototype("missing"));
    }
  }
}
