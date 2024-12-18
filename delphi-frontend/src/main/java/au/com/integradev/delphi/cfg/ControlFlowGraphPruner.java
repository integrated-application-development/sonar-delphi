/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ControlFlowGraphPruner {
  private Block currentBlock;
  private Block exit;
  private List<Block> blocks;

  public static ControlFlowGraph prune(ControlFlowGraph graph) {
    ControlFlowGraphPruner pruner = new ControlFlowGraphPruner();
    pruner.currentBlock = graph.getEntryBlock();
    pruner.exit = graph.getExitBlock();
    pruner.blocks = new ArrayList<>(Lists.reverse(graph.getBlocks()));
    pruner.prune();
    return new ControlFlowGraphImpl(pruner.currentBlock, pruner.exit, pruner.blocks);
  }

  private void prune() {
    Set<Block> inactiveBlocks = new HashSet<>();

    do {
      inactiveBlocks.clear();
      blocks.stream().skip(1).filter(this::isInactive).forEach(inactiveBlocks::add);

      if (inactiveBlocks.isEmpty()) {
        break;
      }

      removeBlocks(inactiveBlocks);
      if (inactiveBlocks.contains(currentBlock)) {
        currentBlock = currentBlock.getSuccessors().iterator().next();
      }

      blocks.forEach(inactiveBlocks::remove);
    } while (!inactiveBlocks.isEmpty());
  }

  private boolean isInactive(Block block) {
    if (block == currentBlock && block.getSuccessors().size() > 1) {
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
            .map(jump -> jump.getSuccessorWithoutJump() != inactiveBlock)
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
