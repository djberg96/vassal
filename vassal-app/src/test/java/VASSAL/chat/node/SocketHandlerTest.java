package VASSAL.chat.node;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SocketHandlerTest {
  private static final int TIMEOUT_MILLIS = 2000;

  private static Socket connectTo(ServerSocket server) throws IOException {
    return new Socket(InetAddress.getLoopbackAddress(), server.getLocalPort());
  }

  @Test
  void closeSendsSignOffAndNotifiesWatcher() throws Exception {
    try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
         Socket client = connectTo(server);
         Socket peer = server.accept();
         BufferedReader peerReader = new BufferedReader(
           new InputStreamReader(peer.getInputStream(), StandardCharsets.UTF_8))) {
      peer.setSoTimeout(TIMEOUT_MILLIS);
      final CountDownLatch closed = new CountDownLatch(1);
      final SocketHandler handler = new SocketHandler(client, new SocketWatcher() {
        @Override
        public void handleMessage(String msg) {
        }

        @Override
        public void socketClosed(SocketHandler handler) {
          closed.countDown();
        }
      });

      handler.start();
      handler.close();

      assertEquals("!BYE", peerReader.readLine());
      assertTrue(closed.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS));
    }
  }

  @Test
  void remoteCloseNotifiesWatcher() throws Exception {
    try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
         Socket client = connectTo(server);
         Socket peer = server.accept()) {
      final CountDownLatch closed = new CountDownLatch(1);
      final SocketHandler handler = new SocketHandler(client, new SocketWatcher() {
        @Override
        public void handleMessage(String msg) {
        }

        @Override
        public void socketClosed(SocketHandler handler) {
          closed.countDown();
        }
      });

      handler.start();
      peer.close();

      assertTrue(closed.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS));
    }
  }

  @Test
  void handlerExceptionDoesNotStopReadThread() throws Exception {
    try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
         Socket client = connectTo(server);
         Socket peer = server.accept();
         BufferedWriter peerWriter = new BufferedWriter(
           new OutputStreamWriter(peer.getOutputStream(), StandardCharsets.UTF_8))) {
      final CountDownLatch delivered = new CountDownLatch(1);
      final AtomicInteger messages = new AtomicInteger();
      final AtomicReference<String> lastMessage = new AtomicReference<>();
      final SocketHandler handler = new SocketHandler(client, new SocketWatcher() {
        @Override
        public void handleMessage(String msg) {
          if (messages.incrementAndGet() == 1) {
            throw new IllegalStateException("first message fails");
          }

          lastMessage.set(msg);
          delivered.countDown();
        }

        @Override
        public void socketClosed(SocketHandler handler) {
        }
      });

      handler.start();
      peerWriter.write("first\nsecond\n!BYE\n");
      peerWriter.flush();

      assertTrue(delivered.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS));
      assertEquals("second", lastMessage.get());
    }
  }
}
