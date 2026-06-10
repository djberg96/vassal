/*
 *
 * Copyright (c) 2009 by Brent Easton
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

import VASSAL.i18n.Resources;

import java.util.List;

/**
 * Code for controlling looping common to both TriggerAction and DoActionButton
 */
public class LoopControl {

  // Limit number of loops before throwing a RecursionLimitException
  public static final int LOOP_LIMIT = 500;

  // Loop Types - saved in buildfile
  public static final String LOOP_COUNTED = "counted"; //$NON-NLS-1$
  public static final String LOOP_WHILE = "while"; //$NON-NLS-1$
  public static final String LOOP_UNTIL = "until"; //$NON-NLS-1$
  public static final List<String> LOOP_TYPES = List.of(LOOP_COUNTED, LOOP_UNTIL, LOOP_WHILE);

  // Localized description of loop types
  public static final List<String> LOOP_TYPE_DESCS = List.of(
      Resources.getString("Editor.LoopControl.repeat_fixed"), //$NON-NLS-1$
      Resources.getString("Editor.LoopControl.repeat_until"), //$NON-NLS-1$
      Resources.getString("Editor.LoopControl.repeat_while")); //$NON-NLS-1$

  public static String[] loopTypes() {
    return LOOP_TYPES.toArray(new String[0]);
  }

  public static String[] loopTypeDescriptions() {
    return LOOP_TYPE_DESCS.toArray(new String[0]);
  }

  /**
   * Convert a Loop Type to a localized description
   *
   * @param type
   *          loop type
   * @return localized description
   */
  public static String loopTypeToDesc(String type) {
    for (int i = 0; i < LOOP_TYPES.size(); i++) {
      if (LOOP_TYPES.get(i).equals(type)) {
        return LOOP_TYPE_DESCS.get(i);
      }
    }
    return LOOP_TYPE_DESCS.get(0);
  }

  /**
   * Convert a localized description of a loop type back to a raw type
   *
   * @param desc
   *          localized description of loop type
   * @return loop type
   */
  public static String loopDescToType(String desc) {
    for (int i = 0; i < LOOP_TYPES.size(); i++) {
      if (LOOP_TYPE_DESCS.get(i).equals(desc)) {
        return LOOP_TYPES.get(i);
      }
      if (LOOP_TYPES.get(i).equals(desc)) {
        return desc;
      }
    }
    return LOOP_TYPES.get(0);
  }

}
