package VASSAL.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class ModuleManagerSocketListenerTest {
  @Test
  void listenerSendsExecutionResponseAndStopsWhenServerSocketCloses() throws Exception {
    try (ServerSocket serverSocket = new ServerSocket(0, 0, InetAddress.getByName(null))) {
      final Thread listener = new Thread(
        new ModuleManagerSocketListener(serverSocket, request -> "handled " + request),
        "test module manager socket listener"
      );
      listener.start();

      try (Socket client = new Socket((String) null, serverSocket.getLocalPort());
           ObjectOutputStream out = new ObjectOutputStream(
             new BufferedOutputStream(client.getOutputStream()));
           BufferedReader in = new BufferedReader(
             new InputStreamReader(new BufferedInputStream(client.getInputStream()), StandardCharsets.UTF_8))) {

        out.writeObject("request");
        out.flush();

        assertEquals("handled request", in.readLine());
      }

      serverSocket.close();
      listener.join(1000);

      assertFalse(listener.isAlive());
    }
  }
}
