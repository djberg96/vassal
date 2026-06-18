package VASSAL.build.module.map.boardPicker.board.mapgrid;

import VASSAL.build.module.map.boardPicker.board.SquareGrid;

import org.junit.jupiter.api.Test;

import java.awt.Point;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SquareGridNumberingTest {
  @Test
  void centerPointUsesGridSnappingForFractionalGridDimensions() {
    final SquareGrid grid = new SquareGrid();
    grid.setOrigin(new Point(24, 24));
    grid.setDx(48.7);
    grid.setDy(51.4);
    grid.setSnapScale(4);
    grid.setAttribute(SquareGrid.SNAP_TO, false);

    final SquareGridNumbering numbering = new SquareGridNumbering();
    numbering.addTo(grid);

    assertEquals(new Point(73, 127), numbering.getCenterPoint(1, 2));
  }
}
