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
package au.com.integradev.delphi.cfg;

import au.com.integradev.delphi.cfg.api.Block;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import au.com.integradev.delphi.cfg.api.Terminated;
import au.com.integradev.delphi.cfg.api.UnconditionalJump;
import au.com.integradev.delphi.cfg.block.BlockImpl;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ControlFlowGraphImpl implements ControlFlowGraph {
  private Block entry;
  private final Block exit;
  private final List<Block> blocks;

  public ControlFlowGraphImpl(Block entry, Block exit, List<Block> blocks) {
    this.entry = entry;
    this.exit = exit;
    this.blocks = blocks;
  }

  @Override
  public Block getEntryBlock() {
    return entry;
  }

  @Override
  public Block getExitBlock() {
    return exit;
  }

  @Override
  public List<Block> getBlocks() {
    return Collections.unmodifiableList(Lists.reverse(blocks));
  }

  /** Removes redundant blocks from the graph and updates their neighbouring blocks. */
  public void prune() {
    Set<Block> inactiveBlocks = new HashSet<>();

    do {
      inactiveBlocks.clear();
      blocks.stream().skip(1).filter(this::isInactive).forEach(inactiveBlocks::add);

      if (inactiveBlocks.isEmpty()) {
        break;
      }

      removeBlocks(inactiveBlocks);
      if (inactiveBlocks.contains(this.entry)) {
        this.entry = this.entry.getSuccessors().iterator().next();
      }

      blocks.forEach(inactiveBlocks::remove);
    } while (!inactiveBlocks.isEmpty());
  }

  private boolean isInactive(Block block) {
    if (block == this.entry && block.getSuccessors().size() > 1) {
      return false;
    }

    return !(block instanceof Terminated)
        && block.getElements().isEmpty()
        && block.getSuccessors().size() == 1;
  }

  private void removeBlocks(Set<Block> inactiveBlocks) {
    for (Block inactiveBlock : inactiveBlocks) {
      Block successor = inactiveBlock.getSuccessors().iterator().next();
      for (Block block : blocks) {
        replaceSuccessorWith(block, inactiveBlock, successor);
      }
    }
    blocks.removeAll(inactiveBlocks);
  }

  private static void replaceSuccessorWith(Block block, Block inactiveBlock, Block successor) {
    if (!block.getSuccessors().contains(inactiveBlock)
        && getAs(block, UnconditionalJump.class)
            .map(jump -> jump.getSuccessorIfRemoved() != inactiveBlock)
            .orElse(true)) {
      return;
    }

    BlockImpl blockImpl = getAs(block, BlockImpl.class).orElseThrow();
    blockImpl.replaceInactiveSuccessor(inactiveBlock, successor);
  }

  private static <T, U> Optional<U> getAs(T subject, Class<U> type) {
    if (type.isInstance(subject)) {
      return Optional.of(type.cast(subject));
    }
    return Optional.empty();
  }
}
