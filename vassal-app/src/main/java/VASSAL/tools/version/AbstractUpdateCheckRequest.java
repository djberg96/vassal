/*
 * Copyright (c) 2008 by Joel Uckelman
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
package VASSAL.tools.version;

import java.io.IOException;

import VASSAL.Info;
import VASSAL.tools.concurrent.BackgroundTasks;

/**
 * @since 3.1.0
 * @author Joel Uckelman
 */
public abstract class AbstractUpdateCheckRequest {
  public final void execute() {
    BackgroundTasks.submit(
      this::isUpdateAvailable,
      update -> succeeded(Boolean.TRUE.equals(update)),
      this::failed
    );
  }

  private Boolean isUpdateAvailable() throws IOException {
    return !VersionUtils.isCurrent(Info.getVersion());
  }

  protected abstract void succeeded(boolean update);

  protected abstract void failed(Throwable e);
}
