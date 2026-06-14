package VASSAL.tools.icon;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.junit.jupiter.api.Test;

import VASSAL.tools.ApplicationIcons;
import VASSAL.tools.DataArchive;

public class IconFactoryTest {
  @Test
  public void imageSourceDescriptionFallsBackToKnownIconPath() {
    assertThat(
      IconFactory.imageSourceDescription(null),
      is(equalTo(DataArchive.IMAGE_DIR + ApplicationIcons.VASSAL_ICON_LARGE))
    );
  }

  @Test
  public void imageSourceDescriptionUsesUrlWhenAvailable()
                                                        throws MalformedURLException {
    final URL url = URI.create("file:/example/images/").toURL();

    assertThat(IconFactory.imageSourceDescription(url), endsWith("/example/images/"));
  }
}
