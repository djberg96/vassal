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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */

package VASSAL.property;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConcurrentPropertySupportTest {
  private static final Property<String> NAME =
    new Property<>("name", String.class);

  @Test
  public void fireChangedNotifiesGeneralAndPropertyListeners() {
    final ConcurrentPropertySupport support = new ConcurrentPropertySupport();
    final Object source = new Object();
    final AtomicReference<String> generalChange = new AtomicReference<>();
    final AtomicReference<String> propertyChange = new AtomicReference<>();

    final PropertyListener<Object> generalListener =
      new PropertyListener<>() {
        @Override
        public <U> void propertyChanged(Object src, Property<U> prop,
                                        U oldVal, U newVal) {
          generalChange.set(src == source
            ? prop.name + ":" + oldVal + "->" + newVal
            : "wrong source");
        }
      };

    final PropertyListener<CharSequence> propertyListener =
      new PropertyListener<>() {
        @Override
        public <U extends CharSequence> void propertyChanged(
          Object src, Property<U> prop, U oldVal, U newVal
        ) {
          propertyChange.set(src == source
            ? prop.name + ":" + oldVal + "->" + newVal
            : "wrong source");
        }
      };

    support.addPropertyListener(generalListener);
    support.addPropertyListener(NAME, propertyListener);

    support.fireChanged(source, NAME, "old", "new");

    assertEquals("name:old->new", generalChange.get());
    assertEquals("name:old->new", propertyChange.get());
  }

  @Test
  public void fireChangedSkipsEquivalentValues() {
    final ConcurrentPropertySupport support = new ConcurrentPropertySupport();
    final AtomicInteger calls = new AtomicInteger();

    support.addPropertyListener(new PropertyListener<>() {
      @Override
      public <U> void propertyChanged(Object src, Property<U> prop,
                                      U oldVal, U newVal) {
        calls.incrementAndGet();
      }
    });

    support.fireChanged(this, NAME, "same", "same");

    assertEquals(0, calls.get());
  }

  @Test
  public void removePropertyListenerStopsGeneralNotifications() {
    final ConcurrentPropertySupport support = new ConcurrentPropertySupport();
    final AtomicInteger calls = new AtomicInteger();
    final PropertyListener<Object> listener = new PropertyListener<>() {
      @Override
      public <U> void propertyChanged(Object src, Property<U> prop,
                                      U oldVal, U newVal) {
        calls.incrementAndGet();
      }
    };

    support.addPropertyListener(listener);
    assertTrue(support.hasListeners());

    support.removePropertyListener(listener);
    support.fireChanged(this, NAME, "old", "new");

    assertFalse(support.hasListeners());
    assertEquals(0, calls.get());
  }

  @Test
  public void removePropertyListenerStopsPropertyNotifications() {
    final ConcurrentPropertySupport support = new ConcurrentPropertySupport();
    final AtomicInteger calls = new AtomicInteger();
    final PropertyListener<CharSequence> listener = new PropertyListener<>() {
      @Override
      public <U extends CharSequence> void propertyChanged(
        Object src, Property<U> prop, U oldVal, U newVal
      ) {
        calls.incrementAndGet();
      }
    };

    support.addPropertyListener(NAME, listener);
    assertTrue(support.hasListeners(NAME));

    support.removePropertyListener(NAME, listener);
    support.fireChanged(this, NAME, "old", "new");

    assertFalse(support.hasListeners(NAME));
    assertEquals(0, calls.get());
  }

  @Test
  public void getPropertyListenersReturnsSnapshot() {
    final ConcurrentPropertySupport support = new ConcurrentPropertySupport();
    final PropertyListener<Object> listener = new PropertyListener<>() {
      @Override
      public <U> void propertyChanged(Object src, Property<U> prop,
                                      U oldVal, U newVal) {
      }
    };

    support.addPropertyListener(listener);

    final List<PropertyListener<Object>> snapshot =
      support.getPropertyListeners();

    support.removePropertyListener(listener);

    assertNotSame(snapshot, support.getPropertyListeners());
    assertEquals(1, snapshot.size());
    assertEquals(listener, snapshot.get(0));
    assertTrue(support.getPropertyListeners().isEmpty());
  }
}
