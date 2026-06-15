/*
 *
 * Copyright (c) 2026 by The VASSAL Development Team
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */

package VASSAL.build.module.map.boardPicker.board;

import java.awt.Point;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HexGridTest {
  @Test
  public void vertexSnappingAtOriginCenterUsesDrawnHexVertex() {
    final TestHexGrid grid = new TestHexGrid();

    assertEquals(new Point(-18, 0), grid.vertexAt(new Point(0, 32)));
  }

  @Test
  public void vertexSnappingAtOffsetCenterUsesDrawnHexVertex() {
    final TestHexGrid grid = new TestHexGrid();

    assertEquals(new Point(37, 32), grid.vertexAt(new Point(55, 64)));
  }

  @Test
  public void snapToHexVertexUsesNearestDrawnHexVertex() {
    final HexGrid grid = new HexGrid();

    assertEquals(new Point(-18, 0), grid.snapToHexVertex(new Point(0, 32)));
  }

  private static final class TestHexGrid extends HexGrid {
    private Point vertexAt(Point p) {
      return new Point(vertexX(p.x, p.y), vertexY(p.x, p.y));
    }
  }
}
