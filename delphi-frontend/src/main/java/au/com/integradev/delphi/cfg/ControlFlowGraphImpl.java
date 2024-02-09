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
import au.com.integradev.delphi.cfg.block.BlockBuilder;
import au.com.integradev.delphi.cfg.block.BlockBuilder.AbstractSuccessor;
import au.com.integradev.delphi.cfg.block.BlockImpl;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.GotoStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.LabelStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonarsource.analyzer.commons.collections.ListUtils;

public class ControlFlowGraphImpl implements ControlFlowGraph {

  private Block currentBlock;
  private final List<Block> blocks = new ArrayList<>();
  private final Deque<Block> exitBlocks = new ArrayDeque<>();
  private final Deque<Block> breakTargets = new ArrayDeque<>();
  private final Deque<Block> continueTargets = new ArrayDeque<>();
  private final Map<String, Block> labelTargets = new HashMap<>();
  private final Map<String, List<UnresolvedLabel>> unresolvedLabels = new HashMap<>();
  private final Deque<TryContext> tryContexts = new ArrayDeque<>();

  public ControlFlowGraphImpl() {
    Block exitBlock = nextBlock(buildNewBlock());
    exitBlocks.add(exitBlock);
    nextBlockTo(exitBlock);
  }

  public void finalise() {
    prune();
    populatePredecessors();
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
        currentBlock = currentBlock.getSuccessorBlocks().iterator().next();
      }

      blocks.forEach(inactiveBlocks::remove);
    } while (!inactiveBlocks.isEmpty());

    // Renumber all remaining blocks
    for (int blockIndex = 0; blockIndex < blocks.size(); blockIndex++) {
      BlockImpl block = getAs(blocks.get(blockIndex), BlockImpl.class).orElseThrow();
      block.setId(blockIndex);
    }
  }

  private boolean isInactive(Block block) {
    if (block == currentBlock && block.getSuccessorBlocks().size() > 1) {
      return false;
    }

    return !(block.getSuccessors() instanceof Terminated)
        && block.getElements().isEmpty()
        && block.getSuccessorBlocks().size() == 1;
  }

  private void removeBlocks(Set<Block> inactiveBlocks) {
    for (Block inactiveBlock : inactiveBlocks) {
      Block successor = inactiveBlock.getSuccessorBlocks().iterator().next();
      for (Block block : blocks) {
        replaceSuccessorWith(block, inactiveBlock, successor);
      }
    }
    blocks.removeAll(inactiveBlocks);
  }

  private void populatePredecessors() {
    for (Block block : blocks) {
      for (Block successor : block.getSuccessorBlocks()) {
        ((BlockImpl) successor).getPredecessors().add(block);
      }
    }
  }

  private void replaceSuccessorWith(Block block, Block inactiveBlock, Block successor) {
    if (!block.getSuccessorBlocks().contains(inactiveBlock)
        && getAs(block.getSuccessors(), UnconditionalJump.class)
            .map(jump -> jump.getSuccessorWithoutJump() != inactiveBlock)
            .orElse(true)) {
      return;
    }

    BlockImpl blockImpl = getAs(block, BlockImpl.class).orElseThrow();
    AbstractSuccessor blockSuccessor =
        getAs(block.getSuccessors(), AbstractSuccessor.class).orElseThrow();
    blockImpl.setSuccessors(blockSuccessor.replaceInactiveBlock(inactiveBlock, successor));
  }

  private <T, U> Optional<U> getAs(T subject, Class<U> type) {
    if (type.isInstance(subject)) {
      return Optional.of(type.cast(subject));
    }
    return Optional.empty();
  }

  @Override
  public Block getEntryBlock() {
    return currentBlock;
  }

  @Override
  public Block getExitBlock() {
    return exitBlocks.peek();
  }

  public Block getBreakTarget() {
    return breakTargets.peek();
  }

  public Block getContinueTarget() {
    return continueTargets.peek();
  }

  public void pushLoopContext(Block continueTarget, Block breakTarget) {
    continueTargets.push(continueTarget);
    breakTargets.push(breakTarget);
  }

  public void popLoopContext() {
    breakTargets.pop();
    continueTargets.pop();
  }

  public void pushExitBlock(Block target) {
    exitBlocks.push(target);
  }

  public void popExitBlock() {
    exitBlocks.pop();
  }

  private static class UnresolvedLabel {
    Block nextBlock;
    Block block;
    DelphiNode node;
  }

  public void addLabel(LabelStatementNode labelNode) {
    NameReferenceNode labelName = labelNode.getNameReference();
    //    addElement(labelName);
    String label = labelName.getImage();

    labelTargets.put(label, currentBlock);
    if (!unresolvedLabels.containsKey(label)) {
      return;
    }

    for (UnresolvedLabel unresolvedLabel : unresolvedLabels.get(label)) {
      updateBlock(
          buildReplacement(unresolvedLabel.block)
              .withJump(unresolvedLabel.node, currentBlock, unresolvedLabel.nextBlock));
    }
  }

  public void addGoto(GotoStatementNode gotoNode) {
    NameReferenceNode labelNode = gotoNode.getNameReference();
    String label = labelNode.getImage();
    if (labelTargets.containsKey(label)) {
      nextBlock(buildNewBlock().withJump(gotoNode, labelTargets.get(label), currentBlock));
      return;
    }

    nextBlockToCurrent();

    unresolvedLabels.putIfAbsent(label, new ArrayList<>());
    UnresolvedLabel unresolvedLabel = new UnresolvedLabel();
    unresolvedLabel.nextBlock = currentBlock;
    unresolvedLabel.block = nextBlockToCurrent();
    unresolvedLabel.node = gotoNode;
    unresolvedLabels.get(label).add(unresolvedLabel);

    addElement(labelNode);
  }

  private static class TryContext {
    LinkedHashMap<Type, Block> catches = new LinkedHashMap<>();
    Block elseBlock = null;
  }

  public void pushTryContext(List<Entry<Type, Block>> catches, Block elseBlock) {
    TryContext tryContext = new TryContext();
    tryContext.catches = new LinkedHashMap<>();
    catches.forEach(entry -> tryContext.catches.put(entry.getKey(), entry.getValue()));
    tryContext.elseBlock = elseBlock;
    tryContexts.push(tryContext);
  }

  public boolean inTryContext() {
    return !tryContexts.isEmpty();
  }

  public Block getCatchTarget(Type exceptionType) {
    if (tryContexts.isEmpty()) {
      return getExitBlock();
    }
    TryContext tryContext = tryContexts.peek();
    return tryContext.catches.keySet().stream()
        .filter(catchType -> isCompatibleType(exceptionType, catchType))
        .findFirst()
        .map(tryContext.catches::get)
        .or(() -> Optional.ofNullable(tryContext.elseBlock))
        .orElse(getExitBlock());
  }

  private boolean isCompatibleType(Type exceptionType, Type catchType) {
    return exceptionType.is(catchType) || exceptionType.isDescendantOf(catchType);
  }

  public Set<Block> getAllCatchTargets() {
    if (tryContexts.isEmpty()) {
      return Collections.emptySet();
    }
    TryContext context = tryContexts.peek();
    Stream<Block> elseOrExit =
        Stream.of(Optional.ofNullable(context.elseBlock).orElse(getExitBlock()));
    return Stream.concat(context.catches.values().stream(), elseOrExit)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  public void popTryContext() {
    tryContexts.pop();
  }

  @Override
  public List<Block> getBlocks() {
    return Collections.unmodifiableList(ListUtils.reverse(blocks));
  }

  public Block getCurrentBlock() {
    return currentBlock;
  }

  public void setCurrentBlock(Block currentBlock) {
    this.currentBlock = blocks.get(currentBlock.getId());
  }

  public void addElement(DelphiNode element) {
    ((BlockImpl) currentBlock).addElement(element);
  }

  public Block nextBlockToCurrent() {
    return nextBlockTo(currentBlock);
  }

  public Block nextBlockTo(Block successor) {
    return nextBlock(buildNewBlock().withSuccessor(successor));
  }

  public BlockBuilder buildNewBlock() {
    return BlockBuilder.newBlock(blocks.size());
  }

  public Block nextBlock(BlockBuilder builder) {
    Block result = builder.build();
    blocks.add(result);
    currentBlock = result;
    return result;
  }

  public BlockBuilder buildReplacement(Block block) {
    return BlockBuilder.newBlock(block.getId());
  }

  public void updateBlock(BlockBuilder builder) {
    Block block = builder.build();
    Block placeholder = blocks.get(block.getId());
    ((BlockImpl) placeholder).setSuccessors(block.getSuccessors());
  }
}
