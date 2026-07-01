/*
 * Copyright (c) 2026 by the Vassal developers
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 */
package VASSAL.tools.jfr;

import jdk.jfr.Event;

public final class JfrEvents {
  private JfrEvents() {
  }

  public static <E extends Event & JfrOutcomeEvent> void commitSuccess(E event) {
    markSuccess(event);
    event.commit();
  }

  public static <E extends Event & JfrOutcomeEvent> void commitOutcome(E event, boolean success) {
    markOutcome(event, success);
    event.commit();
  }

  public static <E extends Event & JfrOutcomeEvent & JfrErrorEvent> void commitFailure(
    E event,
    Throwable error
  ) {
    commitFailure(event, error.getClass().getName());
  }

  public static <E extends Event & JfrOutcomeEvent & JfrErrorEvent> void commitFailure(
    E event,
    String errorType
  ) {
    markFailure(event, errorType);
    event.commit();
  }

  public static void markSuccess(JfrOutcomeEvent event) {
    markOutcome(event, true);
  }

  public static void markOutcome(JfrOutcomeEvent event, boolean success) {
    event.setSuccess(success);
  }

  public static <E extends JfrOutcomeEvent & JfrErrorEvent> void markFailure(E event, Throwable error) {
    markFailure(event, error.getClass().getName());
  }

  public static <E extends JfrOutcomeEvent & JfrErrorEvent> void markFailure(E event, String errorType) {
    event.setSuccess(false);
    event.setErrorType(errorType);
  }
}
