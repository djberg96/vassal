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

@Name("VASSAL.ModuleLoad")
@Label("Module Load")
@Category({"VASSAL", "Module Loading"})
@Description("Initializes and builds a module from its archive.")
public class ModuleLoadEvent extends Event {
  @Label("Archive")
  public String archiveName;

  @Label("Archive Type")
  public String archiveType;

  @Label("Editor Mode")
  public boolean editorMode;

  @Label("Component Count")
  public int componentCount;

  @Label("Success")
  public boolean success;

  @Label("Error Type")
  public String errorType;
}
