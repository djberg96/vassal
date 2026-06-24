/*
 * Copyright (c) 2026 by the Vassal developers
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 */
package VASSAL.tools.jfr;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;

@Name("VASSAL.GameSynchronization")
@Label("Game Synchronization")
@Category({"VASSAL", "Networking"})
@Description("Tracks a requested game synchronization until the received game state is loaded.")
public class GameSynchronizationEvent extends Event {
  @Label("Module")
  public String moduleName;

  @Label("Success")
  public boolean success;

  @Label("Error Type")
  public String errorType;
}
