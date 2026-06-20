/*
 * Copyright (c) 2026 by the Vassal developers
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 */
package VASSAL.tools.http;

import java.io.IOException;

public record HttpResponseData(int status, String body) {
  public boolean isSuccess() {
    return status < 400;
  }

  public String requireSuccess(String serviceName) throws IOException {
    if (!isSuccess()) {
      throw new IOException(serviceName + " returned HTTP " + status + ": " + body); //NON-NLS
    }

    return body;
  }
}
