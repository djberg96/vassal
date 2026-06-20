/*
 *
 * Copyright (c) 2000-2007 by Rodney Kinney
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
package VASSAL.chat;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;

import VASSAL.tools.http.HttpClientService;
import VASSAL.tools.http.HttpResponseData;

/**
 * Performs Get and Post operations to a given URL
 */
public class HttpRequestWrapper {
  private static final HttpClientService HTTP =
    HttpClientService.createDefault(Duration.ofSeconds(60));

  private final String baseURL;

  public HttpRequestWrapper(String baseURL) {
    this.baseURL = baseURL;
  }

  public List<String> doGet(Properties p) throws IOException {
    return doGet("", p); //$NON-NLS-1$
  }

  private URI buildGet(String path, Properties props) throws IOException {
    final String url = baseURL + path;
    final String body = buildFormBody(props);
    if (body.isEmpty()) {
      return URI.create(url);
    }
    return URI.create(url + (url.contains("?") ? "&" : "?") + body); //NON-NLS
  }

  /**
   * Perform a GET request
   * @param path the URL relative to the base URL
   * @param props additional query parameters
   * @return a List of Strings, one for each line in the response
   * @throws IOException
   */
  public List<String> doGet(String path,
                            Properties props) throws IOException {
    final URI uri = buildGet(path, props);
    try {
      return getLinesOk(HTTP.get(uri), 200);
    }
    catch (final IOException e) {
      throw new IOException("Failed to GET " + uri, e); //NON-NLS
    }
  }

  public List<String> doPost(Properties p) throws IOException {
    return doPost("", p); //$NON-NLS-1$
  }

  private String buildFormBody(Properties props) {
    final StringJoiner form = new StringJoiner("&"); //NON-NLS
    if (props != null) {
      for (final Enumeration<?> e = props.keys(); e.hasMoreElements();) {
        final String key = (String) e.nextElement();
        final String value = props.getProperty(key);
        form.add(urlEncode(key) + '=' + urlEncode(value));
      }
    }
    return form.toString();
  }

  private static String urlEncode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  public List<String> doPost(String path,
                             Properties props) throws IOException {
    final HttpResponseData response = HTTP.postForm(URI.create(baseURL + path), buildFormBody(props));
    return getLinesOk(response, 201);
  }

  private List<String> getLinesOk(HttpResponseData response, int okCode) throws IOException {
    if (response.status() == okCode) {
      return response.body().lines().toList();
    }
    else {
      throw new IOException(response.status() + ": " + response.body()); //NON-NLS
    }
  }
}
