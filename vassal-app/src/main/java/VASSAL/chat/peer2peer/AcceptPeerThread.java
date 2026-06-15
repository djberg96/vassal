package VASSAL.chat.peer2peer;

import java.io.IOException;
import java.net.ServerSocket;

import org.litesoft.p2pchat.PendingPeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: Mar 11, 2003
 */
public class AcceptPeerThread extends Thread {
  private static final Logger logger = LoggerFactory.getLogger(AcceptPeerThread.class);

  private boolean running = true;
  private ServerSocket socket;
  private final PendingPeerManager ppm;
  private int port;
  private static final int MAX_ATTEMPTS = 10;

  public AcceptPeerThread(int initialPort, PendingPeerManager ppm) throws IOException {
    this.ppm = ppm;
    for (int i = 0; i < MAX_ATTEMPTS; ++i) {
      port = initialPort + i;
      try {
        socket = new ServerSocket(port);
        break;
      }
      catch (IOException | SecurityException ex) {
        logger.debug("Unable to listen for peers on port {}", port, ex); //NON-NLS
        if (i == MAX_ATTEMPTS - 1) {
          throw new IOException(
            "Unable to listen for peers on ports " + initialPort + "-" + port, //NON-NLS
            ex);
        }
      }
    }
  }

  public int getPort() {
    return port;
  }

  public AcceptPeerThread(ServerSocket socket, PendingPeerManager ppm) {
    this.socket = socket;
    this.ppm = ppm;
  }

  @Override
  public synchronized void start() {
    running = true;
    super.start();
  }

  @Override
  public void run() {
    while (running) {
      try {
        ppm.addNewPeer(socket.accept());
      }
      catch (IOException | RuntimeException ex) {
        if (running) {
          logger.warn("Peer listener stopped after accept failure", ex); //NON-NLS
        }
        halt();
      }
    }
  }

  public void halt() {
    interrupt();
    running = false;
    try {
      socket.close();
    }
    catch (IOException e) {
      logger.debug("Failed to close peer listener socket", e); //NON-NLS
    }
  }
}
