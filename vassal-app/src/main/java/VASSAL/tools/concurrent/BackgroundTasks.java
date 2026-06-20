/*
 * Copyright (c) 2026 by the Vassal developers
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 */
package VASSAL.tools.concurrent;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

public final class BackgroundTasks {
  private static final ExecutorService EXECUTOR = Executors.newThreadPerTaskExecutor(
    Thread.ofVirtual().name("VASSAL-background-", 0).factory() //NON-NLS
  );

  private BackgroundTasks() {
  }

  public static Future<?> submit(Runnable task) {
    Objects.requireNonNull(task, "task"); //NON-NLS
    return EXECUTOR.submit(task);
  }

  public static <T> Future<?> submit(
    Callable<T> task,
    Consumer<? super T> onSuccess,
    Consumer<? super Throwable> onFailure
  ) {
    Objects.requireNonNull(task, "task"); //NON-NLS
    Objects.requireNonNull(onSuccess, "onSuccess"); //NON-NLS
    Objects.requireNonNull(onFailure, "onFailure"); //NON-NLS

    return EXECUTOR.submit(() -> {
      try {
        final T result = task.call();
        SwingUtilities.invokeLater(() -> onSuccess.accept(result));
      }
      catch (Throwable t) {
        if (t instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
        SwingUtilities.invokeLater(() -> onFailure.accept(t));
      }
    });
  }
}
