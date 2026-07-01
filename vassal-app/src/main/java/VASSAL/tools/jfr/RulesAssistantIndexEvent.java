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

@Name("VASSAL.RulesAssistantIndex")
@Label("Rules Assistant Index")
@Category({"VASSAL", "Rules Assistant"})
@Description("Indexes module rules and chart content for Rules Assistant retrieval.")
public class RulesAssistantIndexEvent extends Event implements JfrOutcomeEvent, JfrErrorEvent {
  @Label("Module")
  public String moduleName;

  @Label("Chunks")
  public int chunkCount;

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
