/*
 * Copyright (c) 2026 by the Vassal developers
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 */
package VASSAL.tools.version;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

class AbstractUpdateCheckRequestTest {
  @Test
  void executeReportsAvailableUpdateOnEdt() throws Exception {
    final TestRequest request = new TestRequest(() -> true);

    request.execute();

    assertTrue(request.await());
    assertEquals(Boolean.TRUE, request.update.get());
    assertTrue(request.successOnEdt.get());
  }

  @Test
  void executeReportsCurrentVersionOnEdt() throws Exception {
    final TestRequest request = new TestRequest(() -> false);

    request.execute();

    assertTrue(request.await());
    assertEquals(Boolean.FALSE, request.update.get());
    assertTrue(request.successOnEdt.get());
  }

  @Test
  void executeReportsCheckerFailureOnEdt() throws Exception {
    final TestRequest request = new TestRequest(() -> {
      throw new IOException("network failed"); //NON-NLS
    });

    request.execute();

    assertTrue(request.await());
    assertInstanceOf(IOException.class, request.failure.get());
    assertTrue(request.failureOnEdt.get());
  }

  private static class TestRequest extends AbstractUpdateCheckRequest {
    private final CountDownLatch callback = new CountDownLatch(1);
    private final AtomicReference<Boolean> update = new AtomicReference<>();
    private final AtomicReference<Throwable> failure = new AtomicReference<>();
    private final AtomicBoolean successOnEdt = new AtomicBoolean();
    private final AtomicBoolean failureOnEdt = new AtomicBoolean();

    private TestRequest(UpdateAvailabilityChecker checker) {
      super(checker);
    }

    @Override
    protected void succeeded(boolean update) {
      this.update.set(update);
      successOnEdt.set(SwingUtilities.isEventDispatchThread());
      callback.countDown();
    }

    @Override
    protected void failed(Throwable e) {
      failure.set(e);
      failureOnEdt.set(SwingUtilities.isEventDispatchThread());
      callback.countDown();
    }

    private boolean await() throws InterruptedException {
      return callback.await(5, TimeUnit.SECONDS);
    }
  }
}
