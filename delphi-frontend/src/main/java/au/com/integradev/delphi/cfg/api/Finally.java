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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** A {@code finally} block whose control flow depends on the existence of previous exceptions */
public interface Finally extends Block, Terminated {
  /**
   * Next block in the {@link ControlFlowGraph} without exceptional circumstances
   *
   * @return the successor block of the {@code finally} block
   */
  Block getSuccessor();

  /**
   * Next block in the {@link ControlFlowGraph} if there were exceptional circumstances
   *
   * @return the successor block if the {@code finally} block was reached by an exception
   */
  Block getExceptionSuccessor();

  @Override
  default Set<Block> getSuccessors() {
    Set<Block> successors = new HashSet<>();
    successors.add(getSuccessor());
    successors.add(getExceptionSuccessor());
    return Collections.unmodifiableSet(successors);
  }
}
