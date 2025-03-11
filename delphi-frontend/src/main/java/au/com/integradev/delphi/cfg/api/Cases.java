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

/** A block for the {@code case} statement's behaviour of a possible successor for each arm */
public interface Cases extends Block, Terminated {
  /**
   * All the cases this statement can succeed to in the {@link ControlFlowGraph}
   *
   * @return the set of {@code case} arm successor blocks
   */
  Set<Block> getCaseSuccessors();

  /**
   * Either the fallthrough {@code else} block or the next block in the {@link ControlFlowGraph}
   *
   * @return the fallthrough successor
   */
  Block getFallthroughSuccessor();

  @Override
  default Set<Block> getSuccessors() {
    Set<Block> successors = new HashSet<>(getCaseSuccessors());
    successors.add(getFallthroughSuccessor());
    return Collections.unmodifiableSet(successors);
  }
}
