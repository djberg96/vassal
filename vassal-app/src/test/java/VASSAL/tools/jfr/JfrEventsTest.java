/*
 * Copyright (c) 2026 by the Vassal developers
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 */
package VASSAL.tools.jfr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jdk.jfr.Event;

import org.junit.jupiter.api.Test;

class JfrEventsTest {
  @Test
  void markSuccessSetsOutcome() {
    final TestEvent event = new TestEvent();

    JfrEvents.markSuccess(event);

    assertTrue(event.success);
    assertNull(event.errorType);
  }

  @Test
  void markFailureSetsOutcomeAndErrorType() {
    final TestEvent event = new TestEvent();

    JfrEvents.markFailure(event, new IllegalArgumentException("bad")); //NON-NLS

    assertFalse(event.success);
    assertEquals(IllegalArgumentException.class.getName(), event.errorType);
  }

  @Test
  void markFailureAcceptsExplicitErrorType() {
    final TestEvent event = new TestEvent();

    JfrEvents.markFailure(event, "superseded"); //NON-NLS

    assertFalse(event.success);
    assertEquals("superseded", event.errorType); //NON-NLS
  }

  private static class TestEvent extends Event implements JfrOutcomeEvent, JfrErrorEvent {
    private boolean success;
    private String errorType;

    @Override
    public void setSuccess(boolean success) {
      this.success = success;
    }

    @Override
    public void setErrorType(String errorType) {
      this.errorType = errorType;
    }
  }
}
