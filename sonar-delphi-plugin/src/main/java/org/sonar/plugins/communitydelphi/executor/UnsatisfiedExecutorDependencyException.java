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
package org.sonar.plugins.communitydelphi.executor;

public class UnsatisfiedExecutorDependencyException extends RuntimeException {
  public UnsatisfiedExecutorDependencyException(
      Executor executor, Class<? extends Executor> dependency) {
    super(createMessage(executor, dependency));
  }

  public UnsatisfiedExecutorDependencyException(
      Executor executor, Class<? extends Executor> dependency, Exception cause) {
    super(createMessage(executor, dependency), cause);
  }

  private static String createMessage(Executor executor, Class<? extends Executor> dependency) {
    return String.format(
        "Unsatisfied executor dependency: %s depends on the execution of %s",
        executor.getClass().getSimpleName(), dependency.getSimpleName());
  }
}
