package VASSAL.build.module.map.boardPicker.board.mapgrid;

import org.junit.jupiter.api.Test;

import java.awt.Polygon;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PolygonEditorTest {
  @Test
  void stringToPolygonParsesCoordinatePairs() {
    final Polygon polygon = PolygonEditor.stringToPolygon("1,2; 3,4;5, 6");

    assertEquals(3, polygon.npoints);
    assertEquals(1, polygon.xpoints[0]);
    assertEquals(2, polygon.ypoints[0]);
    assertEquals(3, polygon.xpoints[1]);
    assertEquals(4, polygon.ypoints[1]);
    assertEquals(5, polygon.xpoints[2]);
    assertEquals(6, polygon.ypoints[2]);
  }

  @Test
  void stringToPolygonIgnoresMalformedPairsAndKeepsLaterPoints() {
    final Polygon polygon = PolygonEditor.stringToPolygon("1,2; bad,4; 3,nope; 5,6");

    assertEquals(2, polygon.npoints);
    assertEquals(1, polygon.xpoints[0]);
    assertEquals(2, polygon.ypoints[0]);
    assertEquals(5, polygon.xpoints[1]);
    assertEquals(6, polygon.ypoints[1]);
  }

  @Test
  void stringToPolygonTreatsEmptyInputAsEmptyPolygon() {
    assertEquals(0, PolygonEditor.stringToPolygon("").npoints);
    assertEquals(0, PolygonEditor.stringToPolygon(null).npoints);
  }

  @Test
  void polygonToStringSerializesCoordinatePairs() {
    final Polygon polygon = new Polygon(
      new int[] {1, 3, 5},
      new int[] {2, 4, 6},
      3
    );

    assertEquals("1,2;3,4;5,6", PolygonEditor.polygonToString(polygon));
  }
}
