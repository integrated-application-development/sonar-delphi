/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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
package au.com.integradev.delphi.cfg.api;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A block which may alter the control flow with an exception, e.g., routine invocations in a
 * try-catch
 *
 * <p>Known exceptions with be directly specified with a {@code Linear} block to their target
 */
public interface UnknownException extends Block {
  /**
   * Next block without exceptional circumstances
   *
   * @return the successor block without exceptions
   */
  Block getSuccessor();

  /**
   * Possible exit paths in exceptional circumstance
   *
   * @return the set of successor blocks that could be jumped to in the event of an exception
   */
  Set<Block> getExceptions();

  @Override
  default Set<Block> getSuccessors() {
    return Stream.concat(Stream.of(getSuccessor()), getExceptions().stream())
        .collect(Collectors.toSet());
  }
}
