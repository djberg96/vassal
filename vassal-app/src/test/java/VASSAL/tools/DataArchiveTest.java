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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package VASSAL.tools;

import VASSAL.tools.io.FileArchive;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataArchiveTest {
  private static class TestDataArchive extends DataArchive {
    public TestDataArchive(FileArchive archive) {
      super();
      this.archive = archive;
    }
  }

  @Test
  public void imageNamesComeFromImageDirectory() throws IOException {
    final FileArchive fileArchive = mock(FileArchive.class);
    when(fileArchive.getFiles("images")).thenReturn(List.of(
      "images/counter.png",
      "images/cards/card.gif"
    ));

    final DataArchive archive = new TestDataArchive(fileArchive);

    assertEquals(
      sorted("cards/card.gif", "counter.png"),
      archive.getImageNameSet()
    );
    assertEquals(
      sorted("images/cards/card.gif", "images/counter.png"),
      archive.getImageNameSet(false, true)
    );
  }

  @Test
  public void imageNamesIncludeExtensions() throws IOException {
    final FileArchive baseArchive = mock(FileArchive.class);
    when(baseArchive.getFiles("images")).thenReturn(List.of("images/base.png"));

    final FileArchive extensionArchive = mock(FileArchive.class);
    when(extensionArchive.getFiles("images")).thenReturn(List.of("images/extension.png"));

    final DataArchive archive = new TestDataArchive(baseArchive);
    archive.addExtension(new TestDataArchive(extensionArchive));

    assertEquals(
      sorted("base.png", "extension.png"),
      archive.getImageNameSet()
    );
  }

  @Test
  public void loadClassUsesParentClassLoaderFirst() throws ClassNotFoundException {
    final DataArchive archive = new TestDataArchive(mock(FileArchive.class));

    assertSame(String.class, archive.loadClass("java.lang.String"));
  }

  @Test
  public void loadClassThrowsForMissingClasses() {
    final DataArchive archive = new TestDataArchive(mock(FileArchive.class));

    assertThrows(
      ClassNotFoundException.class,
      () -> archive.loadClass("example.DoesNotExist")
    );
  }

  private static SortedSet<String> sorted(String... values) {
    return new TreeSet<>(List.of(values));
  }
}
