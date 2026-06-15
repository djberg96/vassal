package VASSAL.configure;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

public class HotKeyConfigurerTest {
  @Test
  public void encodeReturnsEmptyStringForNullStroke() {
    assertThat(HotKeyConfigurer.encode(null), is(equalTo("")));
  }

  @Test
  public void decodeRoundTripsEncodedStroke() {
    final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK);

    final KeyStroke decoded = HotKeyConfigurer.decode(HotKeyConfigurer.encode(stroke));

    assertThat(decoded, is(equalTo(stroke)));
  }

  @Test
  public void decodeReturnsNullForMalformedValues() {
    assertNull(HotKeyConfigurer.decode(""));
    assertNull(HotKeyConfigurer.decode("65"));
    assertNull(HotKeyConfigurer.decode("not-a-key,0"));
    assertNull(HotKeyConfigurer.decode("65,not-a-modifier"));
  }
}
