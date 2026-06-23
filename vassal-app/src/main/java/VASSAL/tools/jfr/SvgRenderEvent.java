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

@Name("VASSAL.SvgRender")
@Label("SVG Render")
@Category({"VASSAL", "Image Rendering"})
@Description("Renders an SVG image through JSVG or EchoSVG.")
public class SvgRenderEvent extends Event {
  @Label("Source")
  public String source;

  @Label("Renderer")
  public String renderer;

  @Label("Angle Degrees")
  public double angleDegrees;

  @Label("Scale")
  public double scale;

  @Label("Area Of Interest")
  public boolean areaOfInterest;

  @Label("Rendered Width")
  public int width;

  @Label("Rendered Height")
  public int height;

  @Label("Success")
  public boolean success;
}
