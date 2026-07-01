/*
 *
 * Copyright (c) 2000-2011 by Rodney Kinney, Joel Uckelman
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package VASSAL.tools;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for displaying an external browser window.
 *
 * @author rkinney
 */
public class BrowserSupport {
  private static final Logger logger =
    LoggerFactory.getLogger(BrowserSupport.class);

  public static void openURI(URI uri) {
    //
    // This method is irritatingly complex because java.awt.Desktop seems
    // not to work sometimes on Windows, and sometimes blocks until the
    // program is closed (!) on Linux.
    //

    if (!SystemUtils.IS_OS_LINUX) {
      if (Desktop.isDesktopSupported()) {
        final Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
          try {
            logger.debug("Opening URI with Desktop.browse: {}", uri); //NON-NLS
            desktop.browse(uri);
          }
          catch (IOException e) {
            ReadErrorDialog.error(e, uri.toString());
          }
          return;
        }
      }
    }

    openURIWithExternalLauncher(uri);
  }

  static String externalLauncher() {
    return externalLauncher(SystemUtils.IS_OS_WINDOWS, SystemUtils.IS_OS_MAC);
  }

  static String externalLauncher(boolean windows, boolean mac) {
    if (windows) {
      return "start"; //NON-NLS
    }
    else if (mac) {
      return "open"; //NON-NLS
    }
    else {
      return "xdg-open"; //NON-NLS
    }
  }

  private static void openURIWithExternalLauncher(URI uri) {
    final String uristr = uri.toString();
    final String launcher = externalLauncher();
    logger.debug("Opening URI with external launcher {}: {}", launcher, uristr); //NON-NLS

    final ProcessBuilder pb = new ProcessBuilder(launcher, uristr);
    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
    try {
      pb.start();
    }
    catch (IOException e) {
      ReadErrorDialog.error(e, uristr);
    }
  }

  public static void openURL(URL url) {
    final URI uri;
    try {
      try {
        uri = url.toURI();
      }
      catch (URISyntaxException e) {
        throw new IOException(e);
      }
    }
    catch (IOException e) {
      ReadErrorDialog.error(e, url.toString());
      return;
    }

    openURI(uri);
  }

  public static void openURL(String url) {
    final URI uri;
    try {
      try {
        uri = new URI(url);
      }
      catch (URISyntaxException e) {
        throw new IOException(e);
      }
    }
    catch (IOException e) {
      ReadErrorDialog.error(e, url);
      return;
    }

    openURI(uri);
  }

  private static final HyperlinkListener listener = e -> {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      openURL(e.getURL());
    }
  };

  public static HyperlinkListener getListener() {
    return listener;
  }
}
