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

package VASSAL.tools.image.tilecache;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import VASSAL.tools.concurrent.DaemonThreadFactory;

final class TileThreadPools {
  static final String THREADS_PROPERTY = "vassal.tilecache.threads"; //NON-NLS
  static final int DEFAULT_MAX_THREADS = 8;

  private TileThreadPools() {
  }

  static ExecutorService create(String threadNamePrefix) {
    return Executors.newFixedThreadPool(
      workerCount(),
      new DaemonThreadFactory(threadNamePrefix)
    );
  }

  static int workerCount() {
    return workerCount(
      System.getProperty(THREADS_PROPERTY),
      Runtime.getRuntime().availableProcessors()
    );
  }

  static int workerCount(String configuredThreads, int availableProcessors) {
    if (configuredThreads != null && !configuredThreads.isBlank()) {
      try {
        final int threads = Integer.parseInt(configuredThreads);
        if (threads > 0) {
          return threads;
        }
      }
      catch (NumberFormatException e) {
        // Fall back to the bounded default.
      }
    }

    return defaultWorkerCount(availableProcessors);
  }

  static int defaultWorkerCount(int availableProcessors) {
    return Math.max(1, Math.min(availableProcessors, DEFAULT_MAX_THREADS));
  }
}
