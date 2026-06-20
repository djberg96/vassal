/*
 * Copyright (c) 2026 by the Vassal developers
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 */
package VASSAL.build.module.documentation.ai;

public record RulesChunk(String title, String fileName, int page, String text) {
  public String citation() {
    if (page > 0) {
      return title + ", p. " + page; //NON-NLS
    }
    if (fileName == null || fileName.isBlank()) {
      return title;
    }
    return title + " (" + fileName + ")"; //NON-NLS
  }
}
