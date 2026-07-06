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

import VASSAL.tools.jfr.BackgroundTaskEvent;
import VASSAL.tools.jfr.JfrEvents;

public final class BackgroundTasks {
  private static final ScopedValue<BackgroundTaskContext> CURRENT_CONTEXT =
    ScopedValue.newInstance();

  private static final ExecutorService EXECUTOR = Executors.newThreadPerTaskExecutor(
    Thread.ofVirtual().name("VASSAL-background-", 0).factory() //NON-NLS
  );

  private BackgroundTasks() {
  }

  public static Future<?> submit(Runnable task) {
    Objects.requireNonNull(task, "task"); //NON-NLS
    return EXECUTOR.submit(task);
  }

  public static Future<?> submit(String name, Runnable task) {
    Objects.requireNonNull(task, "task"); //NON-NLS
    return EXECUTOR.submit(() -> runWithContext(name, () -> {
      task.run();
      return null;
    }));
  }

  /**
   * Runs {@code task} on a background virtual thread, then invokes exactly one
   * callback on the Swing event dispatch thread.
   */
  public static <T> Future<?> submitWithCallbacksOnEdt(
    Callable<T> task,
    Consumer<? super T> onSuccess,
    Consumer<? super Throwable> onFailure
  ) {
    return submitWithCallbacksOnEdt(null, task, onSuccess, onFailure);
  }

  /**
   * Runs {@code task} on a background virtual thread with a scoped task
   * context, then invokes exactly one callback on the Swing event dispatch
   * thread.
   */
  public static <T> Future<?> submitWithCallbacksOnEdt(
    String name,
    Callable<T> task,
    Consumer<? super T> onSuccess,
    Consumer<? super Throwable> onFailure
  ) {
    Objects.requireNonNull(task, "task"); //NON-NLS
    Objects.requireNonNull(onSuccess, "onSuccess"); //NON-NLS
    Objects.requireNonNull(onFailure, "onFailure"); //NON-NLS

    return EXECUTOR.submit(() -> {
      try {
        final T result = runWithContext(name, task);
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

  public static BackgroundTaskContext currentContext() {
    return CURRENT_CONTEXT.orElse(BackgroundTaskContext.UNNAMED);
  }

  private static <T> T runWithContext(String name, Callable<T> task) throws Exception {
    return ScopedValue.where(CURRENT_CONTEXT, BackgroundTaskContext.named(name))
      .call(() -> {
        final BackgroundTaskEvent event = new BackgroundTaskEvent();
        event.taskName = currentContext().name();
        event.begin();

        try {
          final T result = task.call();
          JfrEvents.markSuccess(event);
          return result;
        }
        catch (Exception e) {
          JfrEvents.markFailure(event, e);
          throw e;
        }
        catch (Error e) {
          JfrEvents.markFailure(event, e);
          throw e;
        }
        finally {
          event.commit();
        }
      });
  }

  /**
   * @deprecated Use {@link #submitWithCallbacksOnEdt(Callable, Consumer, Consumer)}
   *             so the callback thread boundary is explicit at call sites.
   */
  @Deprecated
  public static <T> Future<?> submit(
    Callable<T> task,
    Consumer<? super T> onSuccess,
    Consumer<? super Throwable> onFailure
  ) {
    return submitWithCallbacksOnEdt(task, onSuccess, onFailure);
  }

  public record BackgroundTaskContext(String name) {
    private static final BackgroundTaskContext UNNAMED =
      new BackgroundTaskContext("unnamed"); //NON-NLS

    private static BackgroundTaskContext named(String name) {
      return name == null || name.isBlank() ? UNNAMED : new BackgroundTaskContext(name);
    }
  }
}
