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

/** A block where the control flow is dictated by a boolean condition, e.g., {@code if} */
public interface Branch extends Block, Terminated {
  /**
   * Next block in the {@link ControlFlowGraph} when the condition is {@code true}
   *
   * @return the successor block if the condition is {@code true}
   */
  Block getTrueBlock();

  /**
   * Next block in the {@link ControlFlowGraph} when the condition is {@code false}
   *
   * @return the successor block if the condition is {@code false}
   */
  Block getFalseBlock();

  @Override
  default Set<Block> getSuccessors() {
    return Set.of(getTrueBlock(), getFalseBlock());
  }
}
