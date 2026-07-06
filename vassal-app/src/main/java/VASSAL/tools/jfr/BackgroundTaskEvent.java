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

@Name("VASSAL.BackgroundTask")
@Label("Background Task")
@Category({"VASSAL", "Background Work"})
@Description("Runs a named background task on VASSAL's shared virtual-thread executor.")
public class BackgroundTaskEvent extends Event implements JfrOutcomeEvent, JfrErrorEvent {
  @Label("Task")
  public String taskName;

  @Label("Success")
  public boolean success;

  @Label("Error Type")
  public String errorType;

  @Override
  public void setSuccess(boolean success) {
    this.success = success;
  }

  @Override
  public void setErrorType(String errorType) {
    this.errorType = errorType;
  }
}
