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

import java.util.List;
import java.util.Set;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;

/** The unit of data within a {@link ControlFlowGraph} */
public interface Block {
  /**
   * Successor blocks of the current block in the {@link ControlFlowGraph}, i.e., the blocks that
   * could be next in the control flow
   *
   * @return the set of successor blocks
   */
  Set<Block> getSuccessors();

  /**
   * Predecessors of the block in the {@link ControlFlowGraph}, i.e., the blocks which could succeed
   * to this block
   *
   * @return the set of predecessor blocks
   */
  Set<Block> getPredecessors();

  /**
   * Elements of the block, e.g., variable names and expressions
   *
   * @return the list of elements
   */
  List<DelphiNode> getElements();
}
