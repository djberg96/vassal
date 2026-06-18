package org.litesoft.p2pchat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class MyInfoTest {
  @Test
  void discoversLocalAddressesForPeerAdvertisement() {
    final MyInfo info = new MyInfo("tester", 5050);

    assertNotNull(info.getAddresses());
    assertFalse(info.getAddresses().isBlank());
  }
}
