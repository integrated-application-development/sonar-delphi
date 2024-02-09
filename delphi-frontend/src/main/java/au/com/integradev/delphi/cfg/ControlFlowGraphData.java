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

import au.com.integradev.delphi.cfg.ControlFlowGraphImpl.Block;
import au.com.integradev.delphi.cfg.ControlFlowGraphImpl.TerminatorKind;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonarsource.analyzer.commons.collections.ListUtils;

public class ControlFlowGraphData {
  public Block currentBlock;
  public final Deque<Block> exitBlocks = new ArrayDeque<>();
  public final List<Block> blocks = new ArrayList<>();
  public final Deque<Block> breakTargets = new ArrayDeque<>();
  public final Deque<Block> continueTargets = new ArrayDeque<>();
  public final Deque<TryStatement> enclosingTry = new ArrayDeque<>();
  public final Deque<Boolean> enclosedByCatch = new ArrayDeque<>();
  public final TryStatement outerTry = new TryStatement();

  public static class TryStatement {

    LinkedHashMap<Type, Block> catches = new LinkedHashMap<>();
    Block defaultCatch = null;

    public void addCatch(Type type, Block catchBlock) {
      catches.put(type, catchBlock);
    }

    public void setDefaultCatch(Block defaultCatch) {
      this.defaultCatch = defaultCatch;
    }

    public Stream<Type> catchTypes() {
      return ListUtils.reverse(new ArrayList<>(catches.keySet())).stream();
    }

    public Optional<Block> findCatch(Predicate<Type> catchFilter) {
      return catchTypes()
          .filter(catchFilter)
          .findFirst()
          .map(type -> catches.get(type))
          .or(() -> Optional.ofNullable(this.defaultCatch));
    }
  }

  private final Map<String, Block> labelBlocks = new HashMap<>();
  private final Map<String, List<Block>> unresolvedLabelTargets = new HashMap<>();

  public Block createBlock(Block successor) {
    Block result = createBlock();
    result.addSuccessor(successor);
    return result;
  }

  public Block createBlock() {
    Block result = new Block(blocks.size());
    blocks.add(result);
    return result;
  }

  public Block exitBlock() {
    return exitBlocks.peek();
  }

  public Block createBranch(DelphiNode terminator, Block trueBranch, Block falseBranch) {
    Block result = createBlock();
    result.setTerminator(TerminatorKind.NODE, terminator);
    result.addFalseSuccessor(falseBranch);
    result.addTrueSuccessor(trueBranch);
    return result;
  }

  public Map<String, Block> getLabelBlocks() {
    return labelBlocks;
  }

  public Map<String, List<Block>> getUnresolvedLabelTargets() {
    return unresolvedLabelTargets;
  }
}
