package VASSAL.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import VASSAL.chat.peer2peer.P2PClientFactory;

import java.util.Properties;

import org.junit.jupiter.api.Test;

class ServerAddressBookTest {
  @Test
  void selectsRequestedP2PServerProperties() {
    final ServerAddressBook addressBook = new ServerAddressBook();
    addressBook.getControls();

    final Properties fedora = new Properties();
    fedora.setProperty(ChatServerFactory.TYPE_KEY, P2PClientFactory.P2P_TYPE);
    fedora.setProperty("description", "Fedora");
    fedora.setProperty(P2PClientFactory.P2P_LISTEN_PORT, "5051");
    fedora.setProperty(P2PClientFactory.P2P_SERVER_PW, "test-password");

    addressBook.setCurrentServer(fedora);

    final Properties selected = addressBook.getCurrentServerProperties();
    assertEquals("Fedora", selected.getProperty("description"));
    assertEquals("5051", selected.getProperty(P2PClientFactory.P2P_LISTEN_PORT));
    assertEquals("test-password", selected.getProperty(P2PClientFactory.P2P_SERVER_PW));
  }
}
