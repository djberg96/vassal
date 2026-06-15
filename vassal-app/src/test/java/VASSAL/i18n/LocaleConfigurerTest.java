/*
 * Copyright 2026 Vassal Development Team
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

package VASSAL.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.Test;

public class LocaleConfigurerTest {
  @Test
  public void localeConstructorStoresEncodedLocale() {
    final LocaleConfigurer configurer = new LocaleConfigurer("locale", "Locale", Locale.CANADA);

    assertEquals("en,CA", configurer.getValueString());
    assertEquals(Locale.CANADA, configurer.getValueLocale());
  }

  @Test
  public void stringConstructorStoresEncodedLocale() {
    final LocaleConfigurer configurer = new LocaleConfigurer("locale", "Locale", "fr,CA");

    assertEquals("fr,CA", configurer.getValueString());
    assertEquals(Locale.CANADA_FRENCH, configurer.getValueLocale());
  }

  @Test
  public void languageOnlyLocaleKeepsEmptyCountryAfterControlsBuild() {
    final LocaleConfigurer configurer = new LocaleConfigurer("locale", "Locale", Locale.FRENCH);

    configurer.getControls();

    assertEquals("fr,", configurer.getValueString());
    assertEquals(Locale.FRENCH, configurer.getValueLocale());
  }

  @Test
  public void localeEncodingRoundTripsLanguageAndCountry() {
    final Locale locale = Locale.of("de", "DE");

    assertEquals(locale, LocaleConfigurer.stringToLocale(LocaleConfigurer.localeToString(locale)));
  }
}
