/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.delphi.executor;

/**
 * Typically if an exception reaches the master Executor, it gets logged and the current Executor
 * gets skipped.
 *
 * <p>As the name implies, a FatalExecutorError is something we don't want to try recovering from.
 * Instead of skipping the current Executor, it will fail the whole scan.
 */
public class FatalExecutorError extends RuntimeException {
  public FatalExecutorError(String message, Exception cause) {
    super(message, cause);
  }
}
