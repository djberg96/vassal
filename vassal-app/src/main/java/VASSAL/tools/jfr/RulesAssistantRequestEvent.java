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

@Name("VASSAL.RulesAssistantRequest")
@Label("Rules Assistant Request")
@Category({"VASSAL", "Rules Assistant"})
@Description("Answers a Rules Assistant question using indexed module rules and chart excerpts.")
public class RulesAssistantRequestEvent extends Event {
  @Label("Module")
  public String moduleName;

  @Label("Provider")
  public String provider;

  @Label("Question Length")
  public int questionLength;

  @Label("Context Chunks")
  public int chunkCount;

  @Label("Success")
  public boolean success;

  @Label("Error Type")
  public String errorType;
}
