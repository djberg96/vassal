package VASSAL.chat.node;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import VASSAL.chat.ChatServerConnection;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;

public class NodeClientTest {

  @Test
  public void malformedCompressedIncomingMessageIsDeliveredUnchanged() {
    final NodeClient client = newNodeClient();
    final AtomicReference<String> incoming = new AtomicReference<>();
    final String message = NodeClient.ZIP_HEADER + "not base64 encoded zip data";

    client.addPropertyChangeListener(
      ChatServerConnection.INCOMING_MSG,
      event -> incoming.set((String) event.getNewValue())
    );

    client.handleMessageFromServer(message);

    assertEquals(message, incoming.get());
  }

  private static NodeClient newNodeClient() {
    return new NodeClient(
      "module",
      "player",
      new NoOpCommandEncoder(),
      "localhost",
      0,
      () -> null
    );
  }

  private static class NoOpCommandEncoder implements CommandEncoder {
    @Override
    public Command decode(String command) {
      return null;
    }

    @Override
    public String encode(Command c) {
      return "";
    }
  }
}
