/*
 * Copyright (c) 2026 by the Vassal developers
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 */
package VASSAL.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BrowserSupportTest {
  @Test
  void externalLauncherUsesWindowsStart() {
    assertEquals("start", BrowserSupport.externalLauncher(true, false)); //NON-NLS
  }

  @Test
  void externalLauncherUsesMacOpen() {
    assertEquals("open", BrowserSupport.externalLauncher(false, true)); //NON-NLS
  }

  @Test
  void externalLauncherUsesXdgOpenForOtherPlatforms() {
    assertEquals("xdg-open", BrowserSupport.externalLauncher(false, false)); //NON-NLS
  }
}
