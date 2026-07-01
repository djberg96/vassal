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

@Name("VASSAL.TileProcessing")
@Label("Tile Processing")
@Category({"VASSAL", "Image Tiling"})
@Description("Slices an image into tiles or reconstructs an image from tiles.")
public class TileProcessingEvent extends Event implements JfrOutcomeEvent, JfrErrorEvent {
  @Label("Operation")
  public String operation;

  @Label("Source")
  public String source;

  @Label("Destination")
  public String destination;

  @Label("Image Width")
  public int imageWidth;

  @Label("Image Height")
  public int imageHeight;

  @Label("Tile Width")
  public int tileWidth;

  @Label("Tile Height")
  public int tileHeight;

  @Label("Tile Count")
  public int tileCount;

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
