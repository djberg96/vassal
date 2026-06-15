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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketHandler {
  private static final Logger logger = LoggerFactory.getLogger(SocketHandler.class);

  private final Socket sock;
  private final SocketWatcher handler;
  private final BufferedReader reader;
  private final BufferedWriter writer;
  private final BlockingQueue<String> writeQueue = new LinkedBlockingQueue<>();
  private volatile boolean isOpen = true;
  private Thread readThread = null;
  private Thread writeThread = null;

  private static final String SIGN_OFF = "!BYE"; //$NON-NLS-1$
  private static final long KEEP_ALIVE_INTERVAL_MINUTES = 2;

  public SocketHandler(Socket sock, SocketWatcher handler) throws IOException {
    this.sock = sock;
    this.handler = handler;
    reader = new BufferedReader(new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8));
    writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.UTF_8));
  }

  public void start() {
    if (readThread == null) {
      readThread = startReadThread();
    }
    if (writeThread == null) {
      writeThread = startWriteThread();
    }
  }

  private Thread startReadThread() {
    final Runnable r = () -> {
      String line;
      try {
        while ((line = reader.readLine()) != null) {
          if (SIGN_OFF.equals(line)) {
            break;
          }
          else if (line.length() > 0) {
            try {
              handler.handleMessage(line);
            }
            catch (Exception e) {
              logger.warn("Socket message handler failed; continuing to read from {}", sock.getInetAddress(), e); //$NON-NLS-1$
            }
          }
        }
      }
      catch (IOException e) {
        if (isOpen) {
          logger.debug("Socket read failed from {}", sock.getInetAddress(), e); //$NON-NLS-1$
        }
      }
      closeSocket();
    };

    final Thread t = new Thread(r, "read " + sock.getInetAddress());
    t.start();
    return t;
  }

  private Thread startWriteThread() {
    final Runnable r = () -> {
      String line;
      try {
        while (true) {
          try {
            line = writeQueue.poll(KEEP_ALIVE_INTERVAL_MINUTES, TimeUnit.MINUTES);
          }
          catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            break;
          }

          if (line != null) {
            if (SIGN_OFF.equals(line)) {
              if (isOpen) {
                writeNext(line);
              }
              break;
            }
            else if (!isOpen) {
              break;
            }

            // send the message we took off the queue
            writeNext(line);
          }
          else {
            if (!isOpen) {
              break;
            }

            // send a keep-alive, since we timed out
            writeNext(""); //$NON-NLS-1$
            logger.trace("Sent socket keep-alive to {}", sock.getInetAddress()); //$NON-NLS-1$
          }
        }
      }
      catch (IOException e) {
        if (isOpen) {
          logger.debug("Socket write failed to {}", sock.getInetAddress(), e); //$NON-NLS-1$
        }
      }
      closeSocket();
    };

    final Thread t = new Thread(r, "write " + sock.getInetAddress());
    t.start();
    return t;
  }

  private void writeNext(String line) throws IOException {
    writer.write(line + '\n');
    writer.flush();
  }

  public void writeLine(String pMessage) {
    if (isOpen) {
      writeQueue.offer(pMessage);
    }
  }

  public void close() {
    writeQueue.offer(SIGN_OFF);
  }

  private synchronized void closeSocket() {
    if (isOpen) {
      isOpen = false;
      writeQueue.offer(SIGN_OFF);

      try {
        writer.close();
      }
      catch (IOException e) {
        logger.debug("Failed to close socket writer for {}", sock.getInetAddress(), e); //$NON-NLS-1$
      }

      try {
        reader.close();
      }
      catch (IOException e) {
        logger.debug("Failed to close socket reader for {}", sock.getInetAddress(), e); //$NON-NLS-1$
      }

      try {
        sock.close();
      }
      catch (IOException e) {
        logger.debug("Failed to close socket for {}", sock.getInetAddress(), e); //$NON-NLS-1$
      }

      handler.socketClosed(this);
    }
  }

  public InetAddress getInetAddress() {
    return sock.getInetAddress();
  }
}
