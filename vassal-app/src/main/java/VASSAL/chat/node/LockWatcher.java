/*
 *
 * Copyright (c) 2000-2007 by Rodney Kinney
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
package VASSAL.chat.node;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches for thread lock on a server.
 * Kills the runtime if unable to establish new connection
 */
public class LockWatcher extends Thread {
  private static final Logger logger = LoggerFactory.getLogger(LockWatcher.class);

  private final long delay;
  private final long timeout;
  private final int port;

  /**
   *
   * @param delay Time in milliseconds between connection attempts
   * @param timeout Wait time in milliseconds to establish a new connection before terminating
   */
  public LockWatcher(long delay, long timeout, int port) {
    this.delay = delay;
    this.timeout = timeout;
    this.port = port;
  }

  @Override
  public void run() {
    while (true) {
      try {
        sleep(delay);
        pingServer();
      }
      catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        logger.debug("Lock watcher interrupted", e); //NON-NLS
        break;
      }
    }
  }

  private void pingServer() {
    try {
      final Socket s = new Socket("localhost", port); //$NON-NLS-1$
      final Thread t = new Thread(new Timeout());
      final SocketWatcher watcher = new SocketWatcher() {
        @Override
        public void handleMessage(String msg) {
          t.interrupt();
        }

        @Override
        public void socketClosed(SocketHandler handler) {
          logger.debug("Server closed socket during lock watcher ping"); //NON-NLS
        }
      };
      final SocketHandler sender = new SocketHandler(s, watcher);
      sender.start();
      t.start();
      sender.writeLine(Protocol.encodeRegisterCommand("pinger", "ping/Main", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      try {
        t.join();
      }
      catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        logger.debug("Interrupted while waiting for lock watcher ping", e); //NON-NLS
      }

      sender.close();
    }
    catch (final IOException e) {
      logger.warn("Unable to ping chat server lock watcher port {}", port, e); //NON-NLS
    }
  }

  private class Timeout implements Runnable {
    @Override
    public void run() {
      try {
        sleep(timeout);
        logger.error("No response from server in {} seconds; terminating process", timeout / 1000.0); //NON-NLS
        System.exit(0);
      }
      catch (final InterruptedException e) {
        // Interrupt means response received from server
        logger.debug("Lock watcher ping response received", e); //NON-NLS
      }
    }
  }
}
