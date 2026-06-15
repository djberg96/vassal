package VASSAL.chat.node;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Properties;
import org.junit.jupiter.api.Test;

public class ProtocolTest {
  @Test
  public void decodeRoomsInfoReturnsNullForNonRoomInfoCommand() {
    assertNull(Protocol.decodeRoomsInfo("NOT_ROOM_INFO\troom=value"));
  }

  @Test
  public void decodeRoomsInfoReturnsNullForMalformedProperties() {
    assertNull(Protocol.decodeRoomsInfo(Protocol.ROOM_INFO + "room=\\uNotHex"));
  }

  @Test
  public void decodeRoomsInfoReturnsPropertiesForRoomInfoCommand() {
    final Properties decoded = Protocol.decodeRoomsInfo(Protocol.ROOM_INFO + "room=value");

    assertThat(decoded.getProperty("room"), is(equalTo("value")));
  }
}
