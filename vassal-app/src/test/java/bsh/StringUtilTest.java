package bsh;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;

public class StringUtilTest {

  @Test
  public void splitsStringsByDelimiterCharacters() {
    assertThat(StringUtil.split("one.two:three", ".:"), arrayContaining("one", "two", "three"));
  }

  @Test
  public void findsMaximumCommonPrefix() {
    assertThat(StringUtil.maxCommonPrefix("alpha", "alpine"), is("alp"));
  }

  @Test
  public void formatsMethodSignatures() {
    assertThat(
      StringUtil.methodString("call", new Class<?>[] { String.class, null, Integer.TYPE }),
      is("call( java.lang.String, null, int )")
    );
  }

  @Test
  public void normalizesClassNames() {
    assertThat(StringUtil.normalizeClassName(String[].class), is("java.lang.String []"));
  }
}
