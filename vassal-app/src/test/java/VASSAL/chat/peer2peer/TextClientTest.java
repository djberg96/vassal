package VASSAL.chat.peer2peer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import VASSAL.chat.Room;
import VASSAL.chat.SimplePlayer;
import VASSAL.chat.SimpleRoom;

public class TextClientTest {
  @Test
  public void reportReturnsEmptyStringForNoRooms() {
    assertEquals("", TextClient.report(new Room[0]));
  }

  @Test
  public void reportIncludesEmptyRooms() {
    assertEquals(
      "Waiting Room: \n",
      TextClient.report(new Room[] {
        new SimpleRoom("Waiting Room")
      })
    );
  }

  @Test
  public void reportIncludesRoomsAndPlayersInOrder() {
    assertEquals(
      "Lobby: Alice, Bob\n" +
      "Table 1: Carol\n",
      TextClient.report(new Room[] {
        room("Lobby", "Alice", "Bob"),
        room("Table 1", "Carol")
      })
    );
  }

  private static Room room(String name, String... playerNames) {
    final SimpleRoom room = new SimpleRoom(name);
    for (final String playerName : playerNames) {
      room.addPlayer(new SimplePlayer(playerName));
    }
    return room;
  }
}
