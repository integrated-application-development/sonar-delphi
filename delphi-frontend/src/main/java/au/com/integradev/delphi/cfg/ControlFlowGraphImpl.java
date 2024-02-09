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

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ForStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.RepeatStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.WhileStatementNode;
import org.sonar.plugins.communitydelphi.api.cfg.ControlFlowGraph;
import org.sonarsource.analyzer.commons.collections.ListUtils;

public class ControlFlowGraphImpl implements ControlFlowGraph {
  private final ControlFlowGraphData data;

  public ControlFlowGraphImpl(List<? extends DelphiNode> nodes) {
    this.data = new ControlFlowGraphData();
    data.exitBlocks.add(data.createBlock());
    data.currentBlock = data.createBlock(exitBlock());
    data.enclosingTry.add(data.outerTry);
    data.enclosedByCatch.push(false);

    CFGBuilder.build(nodes, data);
    prune();
    computePredecessors(data.blocks);
  }

  private void prune() {
    List<Block> inactiveBlocks = new ArrayList<>();
    boolean first = true;
    for (Block block : data.blocks) {
      if (!first && isInactive(block)) {
        inactiveBlocks.add(block);
      }
      first = false;
    }
    if (!inactiveBlocks.isEmpty()) {
      removeInactiveBlocks(inactiveBlocks);
      if (inactiveBlocks.contains(data.currentBlock)) {
        data.currentBlock = data.currentBlock.successors.iterator().next();
      }
      int id = 0;
      for (Block block : data.blocks) {
        block.id = id;
        id += 1;
      }
      inactiveBlocks.removeAll(data.blocks);
      if (!inactiveBlocks.isEmpty()) {
        prune();
      }
    }
  }

  private static void computePredecessors(List<Block> blocks) {
    for (Block b : blocks) {
      for (Block successor : b.successors) {
        successor.predecessors.add(b);
      }
      for (Block successor : b.exceptions) {
        successor.predecessors.add(b);
      }
    }
    cleanupUnfeasibleBreakPaths(blocks);
  }

  private static void cleanupUnfeasibleBreakPaths(List<Block> blocks) {
    for (Block block : blocks) {
      Set<Block> happyPathPredecessor =
          block.predecessors.stream()
              .filter(p -> !p.exceptions.contains(block))
              .collect(Collectors.toSet());
      if (block.isFinallyBlock && happyPathPredecessor.size() == 1) {
        Block pred = happyPathPredecessor.iterator().next();
        if (pred.terminator != null && pred.terminatorKind == TerminatorKind.BREAK) {
          Set<Block> successors =
              block.successors.stream()
                  .map(suc -> isLoop(suc) ? getAfterLoopBlock(suc) : suc)
                  .filter(Objects::nonNull)
                  .collect(Collectors.toSet());
          block.successors.clear();
          block.successors.addAll(successors);
        }
      }
    }
  }

  private static boolean isLoop(Block successor) {
    return successor.terminator != null
        && (successor.terminator instanceof WhileStatementNode
            || successor.terminator instanceof RepeatStatementNode
            || successor.terminator instanceof ForStatementNode);
  }

  @CheckForNull
  private static Block getAfterLoopBlock(Block loop) {
    if (loop.falseBlock != null) {
      return loop.falseBlock;
    }
    return loop.successorWithoutJump;
  }

  private boolean isInactive(Block block) {
    if (block.equals(data.currentBlock) && block.successors.size() > 1) {
      return false;
    }
    return block.isInactive();
  }

  private void removeInactiveBlocks(List<Block> inactiveBlocks) {
    for (Block inactiveBlock : inactiveBlocks) {
      for (Block block : data.blocks) {
        block.prune(inactiveBlock);
      }
    }
    data.blocks.removeAll(inactiveBlocks);
  }

  public Block entryBlock() {
    return data.currentBlock;
  }

  public List<Block> blocks() {
    return ListUtils.reverse(data.blocks);
  }

  public Block exitBlock() {
    return data.exitBlocks.peek();
  }

  public interface DelphiBlock {
    int id();

    Block exitBlock();

    List<DelphiNode> elements();

    DelphiNode terminator();

    Set<Block> successors();
  }

  public enum TerminatorKind {
    BREAK,
    CONTINUE,
    EXIT,
    HALT,
    RAISE,
    NODE,
  }

  public static class Block implements DelphiBlock {
    private int id;
    private final List<DelphiNode> elements = new ArrayList<>();
    private final Set<Block> successors = new LinkedHashSet<>();
    private final Set<Block> predecessors = new LinkedHashSet<>();
    private final Set<Block> exceptions = new LinkedHashSet<>();

    private DelphiNode terminator;
    private TerminatorKind terminatorKind;
    private Block trueBlock;
    private Block falseBlock;
    private Block exitBlock;
    private Block successorWithoutJump;
    private boolean isExceptBlock = false;
    private boolean isFinallyBlock;
    private boolean isSink = false;
    private boolean isElseBlock = false;

    // case group node

    public Block(int id) {
      this.id = id;
    }

    public void addSuccessor(Block successor) {
      successors.add(successor);
    }

    @Override
    public int id() {
      return id;
    }

    @Override
    public List<DelphiNode> elements() {
      return ListUtils.reverse(elements);
    }

    public Block trueBlock() {
      return trueBlock;
    }

    public Block falseBlock() {
      return falseBlock;
    }

    @Override
    public Block exitBlock() {
      return exitBlock;
    }

    public boolean isFinallyBlock() {
      return isFinallyBlock;
    }

    public boolean isExceptBlock() {
      return isExceptBlock;
    }

    public boolean isElseBlock() {
      return isElseBlock;
    }

    public boolean isSink() {
      return isSink;
    }

    public void addTrueSuccessor(Block successor) {
      if (trueBlock != null) {
        throw new IllegalStateException("Attempt to re-assign a true successor");
      }
      successors.add(successor);
      trueBlock = successor;
    }

    public void addFalseSuccessor(Block successor) {
      if (falseBlock != null) {
        throw new IllegalStateException("Attempt to re-assign a false successor");
      }
      successors.add(successor);
      falseBlock = successor;
    }

    public void addExitSuccessor(Block block) {
      successors.add(block);
      exitBlock = block;
    }

    public void addException(Block exceptBlock) {
      exceptions.add(exceptBlock);
    }

    public void addExceptions(Collection<Block> exceptBlocks) {
      exceptions.addAll(exceptBlocks);
    }

    public void addElement(DelphiNode element) {
      elements.add(element);
    }

    public void setTerminator(TerminatorKind kind, DelphiNode terminator) {
      this.terminatorKind = kind;
      this.terminator = terminator;
    }

    public void setExitBlock(Block exitBlock) {
      this.exitBlock = exitBlock;
    }

    public void setSink(boolean isSink) {
      this.isSink = isSink;
    }

    public void setExceptBlock(boolean exceptBlock) {
      this.isExceptBlock = exceptBlock;
    }

    public void setFinallyBlock(boolean finallyBlock) {
      this.isFinallyBlock = finallyBlock;
    }

    public void setElseBlock(boolean elseBlock) {
      this.isElseBlock = elseBlock;
    }

    public void setSuccessorWithoutJump(Block successorWithoutJump) {
      this.successorWithoutJump = successorWithoutJump;
    }

    @Override
    public DelphiNode terminator() {
      return terminator;
    }

    @Override
    public Set<Block> successors() {
      return successors;
    }

    public Set<Block> predecessors() {
      return predecessors;
    }

    public Set<Block> exceptions() {
      return exceptions;
    }

    public boolean isInactive() {
      return terminator == null && elements.isEmpty() && successors.size() == 1;
    }

    public boolean isMethodExitBlock() {
      return this.successors().isEmpty();
    }

    public Block successorWithoutJump() {
      return successorWithoutJump;
    }

    private void prune(Block inactiveBlock) {
      boolean hasUniqueSuccessor = inactiveBlock.successors.size() == 1;
      if (inactiveBlock.equals(trueBlock)) {
        Preconditions.checkArgument(
            hasUniqueSuccessor, "True successor must be replaced by a unique successor!");
        trueBlock = inactiveBlock.successors.iterator().next();
      }
      if (inactiveBlock.equals(falseBlock)) {
        Preconditions.checkArgument(
            hasUniqueSuccessor, "False successor must be replaced by a unique successor!");
        falseBlock = inactiveBlock.successors.iterator().next();
      }
      if (inactiveBlock.equals(successorWithoutJump)) {
        Preconditions.checkArgument(
            hasUniqueSuccessor,
            "SuccessorWithoutJump successor must be replaced by a unique successor!");
        successorWithoutJump = inactiveBlock.successors.iterator().next();
      }
      if (successors.remove(inactiveBlock)) {
        successors.addAll(inactiveBlock.successors);
      }
      if (exceptions.remove(inactiveBlock)) {
        exceptions.addAll(inactiveBlock.exceptions);
        exceptions.addAll(inactiveBlock.successors);
      }
      if (inactiveBlock.equals(exitBlock)) {
        exitBlock = inactiveBlock.successors.iterator().next();
      }
    }
  }
}
