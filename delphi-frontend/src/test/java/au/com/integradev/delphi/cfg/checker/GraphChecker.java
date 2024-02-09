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
package au.com.integradev.delphi.cfg.checker;

import static org.assertj.core.api.Assertions.*;

import au.com.integradev.delphi.cfg.api.Block;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import au.com.integradev.delphi.cfg.block.BlockImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GraphChecker {
  private final List<BlockChecker> checkers = new ArrayList<>();

  public static GraphChecker checker(BlockChecker... blocks) {
    return new GraphChecker(blocks);
  }

  private GraphChecker(BlockChecker... checkers) {
    Collections.addAll(this.checkers, checkers);
  }

  public void check(final ControlFlowGraph cfg) {
    assertThat(cfg.getBlocks()).as("block count").hasSize(checkers.size() + 1);
    final Iterator<BlockChecker> checkerIterator = checkers.iterator();

    List<Block> blocks = new ArrayList<>(cfg.getBlocks());
    final Block exitBlock = blocks.remove(blocks.size() - 1);
    for (Block block : blocks) {
      checkerIterator.next().check(block);
      int blockId = ((BlockImpl) block).getId();
      checkLinkedBlocks("Successor of B" + blockId, cfg.getBlocks(), block.getSuccessors());
      checkLinkedBlocks("Predecessor of B" + blockId, cfg.getBlocks(), block.getPredecessors());
    }
    assertThat(exitBlock.getElements()).isEmpty();
    assertThat(exitBlock.getSuccessors()).isEmpty();
    assertThat(cfg.getBlocks())
        .withFailMessage("CFG entry block is no longer in the list of blocks!")
        .contains(cfg.getEntryBlock());
  }

  private void checkLinkedBlocks(String type, List<Block> blocks, Set<Block> linkedBlocks) {
    for (Block block : linkedBlocks) {
      assertThat(block)
          .withFailMessage(
              type + ", block B" + ((BlockImpl) block).getId() + " is missing from CFG's blocks")
          .isIn(blocks);
    }
  }
}
