package VASSAL.configure;

import java.awt.Point;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class LongConfigurerTest {

  @Test
  public void testLongConfigurer() {
    final String key = "key"; // NON-NLS
    final String name = "name"; // NON-NLS
    final Long value = 42L;
    final Long dflt = 24L;

    final LongConfigurer config = new LongConfigurer(key, name, value);

    // check gui builds
    config.getControls();

    //Check basic functionality
    assertThat(config.getKey(), is(equalTo(key)));
    assertThat(config.getName(), is(equalTo(name)));
    assertThat(config.getValue(), is(equalTo(value)));
    assertThat(config.getLongValue(dflt), is(equalTo(value)));
    assertThat(config.getValueString(), is(equalTo(String.valueOf(value))));

    // Trying to set value to a non-number string should quietly fail and not change value
    config.setValue("xyzzy"); // NON-NLS
    assertThat(config.getLongValue(dflt), is(equalTo(value)));

    // Setting value to a long String should change value
    config.setValue("52");
    assertThat(config.getLongValue(dflt), is(equalTo(52L)));

    // Setting value to rubbish will allow default value to be returned.
    config.setValue(new Point(0, 0));
    assertThat(config.getLongValue(dflt), is(equalTo(dflt)));
  }
}
