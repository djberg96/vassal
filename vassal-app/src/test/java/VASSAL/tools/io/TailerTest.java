/*
 *
 * Copyright (c) 2010 by Joel Uckelman
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

package VASSAL.tools.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TailerTest {

  private static final String EXISTS = "src/test/resources/TailerTest.txt";
  private static final String NOTEXISTS = "src/test/resources/notexists";

  @Test
  public void testGetFile() {
    final File file = new File(EXISTS);
    final Tailer tailer = new Tailer(file);
    assertEquals(file, tailer.getFile());
  }

  @Test
  public void testIsTailingTrue() throws IOException {
    final File file = new File(EXISTS);
    final Tailer tailer = new Tailer(file);
    tailer.start();
    assertTrue(tailer.isTailing());
    tailer.stop();
  }

  @Test
  public void testIsTailingFalse() {
    final File file = new File(EXISTS);
    final Tailer tailer = new Tailer(file);
    assertFalse(tailer.isTailing());
  }

  @Test
  public void testNoFile() {
    final File file = new File(NOTEXISTS);
    final Tailer tailer = new Tailer(file);
    assertThrows(IOException.class, () -> tailer.start());
  }

  @Test
  public void testDirectory() {
    final File file = new File(".");
    final Tailer tailer = new Tailer(file);
    assertThrows(IOException.class, () -> tailer.start());
  }

  @Test
  public void testTailer() throws InterruptedException, IOException {
    final File file = new File(EXISTS);

    final StringBuilder sb_tailer = new StringBuilder();

    final Tailer tailer = new Tailer(file);
    tailer.addEventListener((src, str) -> sb_tailer.append(str));
    tailer.start();

    // give the Tailer time to work
    Thread.sleep(1000L);

    tailer.stop();

    final String actual = sb_tailer.toString().replace("\r\n", "\n");
    final String expected = Files.readString(file.toPath())
                                 .replace("\r\n", "\n")
                                 .substring(0, actual.length());

    // compare whatever the Tailer had time to read
    assertEquals(expected, actual);
  }

  @Test
  public void testErrorListenerLifecycle() {
    final File file = new File(EXISTS);
    final Tailer tailer = new Tailer(file);

    assertFalse(tailer.hasErrorListeners());

    tailer.addErrorListener((src, error) -> { });

    assertTrue(tailer.hasErrorListeners());
    assertEquals(1, tailer.getErrorListeners().size());

    tailer.removeErrorListener(tailer.getErrorListeners().get(0));

    assertFalse(tailer.hasErrorListeners());
  }

  @Test
  public void testMonitorErrorsNotifyErrorListeners()
    throws IOException, InterruptedException {

    final File file = File.createTempFile("tailer-error", ".txt");
    file.deleteOnExit();
    Files.writeString(file.toPath(), "start\n");

    final Tailer tailer = new Tailer(file, 10L);
    final CountDownLatch errorReceived = new CountDownLatch(1);
    final AtomicReference<Object> source = new AtomicReference<>();
    final AtomicReference<Exception> error = new AtomicReference<>();

    tailer.addErrorListener((src, ex) -> {
      source.set(src);
      error.set(ex);
      errorReceived.countDown();
    });

    try {
      tailer.start();

      final Thread monitor = waitForMonitorThread(tailer);
      monitor.interrupt();

      assertTrue(errorReceived.await(5, TimeUnit.SECONDS));
      assertSame(tailer, source.get());
      assertInstanceOf(InterruptedException.class, error.get());
      assertEventuallyStopped(tailer);
    }
    finally {
      tailer.stop();
      Files.deleteIfExists(file.toPath());
    }
  }

  private static Thread waitForMonitorThread(Tailer tailer)
    throws InterruptedException {

    final String threadName = "tailing " + tailer.getFile().getAbsolutePath();
    final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

    while (System.nanoTime() < deadline) {
      for (final Thread thread : Thread.getAllStackTraces().keySet()) {
        if (threadName.equals(thread.getName())) {
          return thread;
        }
      }

      Thread.sleep(10L);
    }

    fail("Timed out waiting for Tailer monitor thread");
    return null;
  }

  private static void assertEventuallyStopped(Tailer tailer)
    throws InterruptedException {

    final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

    while (System.nanoTime() < deadline) {
      if (!tailer.isTailing()) {
        return;
      }

      Thread.sleep(10L);
    }

    fail("Timed out waiting for Tailer to stop");
  }
}
