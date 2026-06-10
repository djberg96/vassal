/*
 * Copyright 2026 Vassal Development Team
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package VASSAL.counters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

public class MatTest {
  @Test
  public void getMatContentsReturnsCargoPieces() {
    final Mat mat = Decorator.create(Mat::new, Mat.ID + "mat;;", new BasicPiece());
    final MatCargo cargo = Decorator.create(MatCargo::new, MatCargo.ID + ";true", new BasicPiece());

    mat.addCargo(cargo);

    assertThat(Mat.getMatContents(mat), contains(cargo));
  }

  @Test
  public void getMatContentsReturnsEmptyListForMissingContents() {
    assertThat(Mat.getMatContents(null), is(empty()));

    final GamePiece piece = mock(GamePiece.class);
    when(piece.getProperty(Mat.MAT_CONTENTS)).thenReturn("not a list");

    assertThat(Mat.getMatContents(piece), is(empty()));
  }

  @Test
  public void getMatContentsIgnoresNonPieceContents() {
    final BasicPiece cargo = new BasicPiece();
    final GamePiece piece = mock(GamePiece.class);
    when(piece.getProperty(Mat.MAT_CONTENTS)).thenReturn(List.of("not a piece", cargo));

    assertThat(Mat.getMatContents(piece), contains(cargo));
  }
}
