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

/** A block which has particular alteration in control flow, e.g., {@code goto} */
public interface UnconditionalJump extends Block, Terminated {
  /**
   * The jumping target block in the {@link ControlFlowGraph}
   *
   * @return the successor block of the jump
   */
  Block getSuccessor();

  /**
   * The next block in the {@link ControlFlowGraph} if the jump were to be removed
   *
   * @return the successor block if this control flow alteration were to be removed
   */
  Block getSuccessorIfRemoved();

  @Override
  default Set<Block> getSuccessors() {
    return Set.of(getSuccessor());
  }
}
