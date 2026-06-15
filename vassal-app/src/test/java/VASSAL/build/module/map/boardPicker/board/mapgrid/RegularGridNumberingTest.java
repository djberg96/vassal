package VASSAL.build.module.map.boardPicker.board.mapgrid;

import VASSAL.build.Buildable;
import VASSAL.build.module.map.boardPicker.board.MapGrid.BadCoordsException;
import org.junit.jupiter.api.Test;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegularGridNumberingTest {
  private static class TestNumbering extends RegularGridNumbering {
    @Override
    public void addTo(Buildable parent) {
    }

    @Override
    public void removeFrom(Buildable parent) {
    }

    @Override
    public void draw(Graphics g, Rectangle bounds, Rectangle visibleRect, double scale, boolean reversed) {
    }

    @Override
    public int getRow(Point p) {
      return p.y;
    }

    @Override
    public int getColumn(Point p) {
      return p.x;
    }

    @Override
    public Point getCenterPoint(int col, int row) {
      return new Point(col, row);
    }

    @Override
    protected JComponent getGridVisualizer() {
      return new JPanel();
    }
  }

  @Test
  void getLocationReturnsCenterPointForMatchingLocation() throws BadCoordsException {
    final RegularGridNumbering numbering = new TestNumbering();

    assertEquals(new Point(2, 3), numbering.getLocation("0304"));
  }

  @Test
  void getLocationThrowsBadCoordsExceptionForInvalidLocation() {
    final RegularGridNumbering numbering = new TestNumbering();

    assertThrows(BadCoordsException.class, () -> numbering.getLocation("not a location"));
  }
}
