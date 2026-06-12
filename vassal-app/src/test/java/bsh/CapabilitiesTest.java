package bsh;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class CapabilitiesTest {

  @Test
  public void classExistsCachesSuccessfulLookupsByName() throws Exception {
    final Map<String, Class<?>> cache = classCache();
    final String className = String.class.getName();
    cache.remove(className);

    try {
      assertThat(Capabilities.classExists(className), is(true));
      assertThat(cache.get(className), is(String.class));
    }
    finally {
      cache.remove(className);
    }
  }

  @Test
  public void classExistsDoesNotCacheFailedLookups() throws Exception {
    final Map<String, Class<?>> cache = classCache();
    final String className = "bsh.NoSuchClass";
    cache.remove(className);

    assertThat(Capabilities.classExists(className), is(false));
    assertThat(cache.get(className), is(nullValue()));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Class<?>> classCache() throws ReflectiveOperationException {
    final Field classes = Capabilities.class.getDeclaredField("classes");
    classes.setAccessible(true);
    return (Map<String, Class<?>>) classes.get(null);
  }
}
