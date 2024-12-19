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

import static au.com.integradev.delphi.cfg.block.BlockBuilder.newBlock;

import au.com.integradev.delphi.cfg.api.Block;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import au.com.integradev.delphi.cfg.block.BlockBuilder;
import au.com.integradev.delphi.cfg.block.BlockImpl;
import au.com.integradev.delphi.cfg.block.BuilderBlock;
import com.google.common.collect.Lists;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
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

public class ControlFlowGraphBuilder {
  private BuilderBlock currentBlock;
  private final List<BuilderBlock> blocks = new ArrayList<>();
  private final Deque<BuilderBlock> exitBlocks = new ArrayDeque<>();
  private final Deque<BuilderBlock> breakTargets = new ArrayDeque<>();
  private final Deque<BuilderBlock> continueTargets = new ArrayDeque<>();
  private final Map<String, BuilderBlock> labelTargets = new HashMap<>();
  private final Map<String, List<UnresolvedLabel>> unresolvedLabels = new HashMap<>();
  private final Deque<TryContext> tryContexts = new ArrayDeque<>();

  public ControlFlowGraphBuilder() {
    BuilderBlock exitBlock = nextBlock(newBlock());
    exitBlocks.add(exitBlock);
    nextBlockTo(exitBlock);
  }

  public ControlFlowGraph build() {
    Map<BuilderBlock, Block> map = new LinkedHashMap<>();
    for (BuilderBlock block : blocks) {
      map.put(block, block.buildBlock());
    }
    for (BuilderBlock block : blocks) {
      block.updateBlockData(map);
    }

    ControlFlowGraph cfg =
        new ControlFlowGraphImpl(
                map.get(currentBlock), map.get(exitBlocks.peek()), new ArrayList<>(map.values()))
            .pruned();

    populatePredecessors(cfg);
    populateIds(cfg);

    return cfg;
  }

  private static void populatePredecessors(ControlFlowGraph cfg) {
    for (Block block : cfg.getBlocks()) {
      for (Block successor : block.getSuccessors()) {
        ((BlockImpl) successor).addPredecessor(block);
      }
    }
  }

  private static void populateIds(ControlFlowGraph cfg) {
    List<Block> blocks = Lists.reverse(cfg.getBlocks());
    for (int blockId = 0; blockId < blocks.size(); blockId++) {
      ((BlockImpl) blocks.get(blockId)).setId(blockId);
    }
  }

  public BuilderBlock getExitBlock() {
    return exitBlocks.peek();
  }

  public BuilderBlock getBreakTarget() {
    return breakTargets.peek();
  }

  public BuilderBlock getContinueTarget() {
    return continueTargets.peek();
  }

  public void pushLoopContext(BuilderBlock continueTarget, BuilderBlock breakTarget) {
    continueTargets.push(continueTarget);
    breakTargets.push(breakTarget);
  }

  public void popLoopContext() {
    breakTargets.pop();
    continueTargets.pop();
  }

  public void pushExitBlock(BuilderBlock target) {
    exitBlocks.push(target);
  }

  public void popExitBlock() {
    exitBlocks.pop();
  }

  private static class UnresolvedLabel {
    BuilderBlock nextBlock;
    BuilderBlock block;
    DelphiNode node;
  }

  public void addLabel(LabelStatementNode labelNode) {
    NameReferenceNode labelName = labelNode.getNameReference();
    String label = labelName.getImage();

    labelTargets.put(label, currentBlock);
    if (!unresolvedLabels.containsKey(label)) {
      return;
    }

    // When they are processed, all the previously unresolved targets must be updated
    for (UnresolvedLabel unresolvedLabel : unresolvedLabels.get(label)) {
      unresolvedLabel.block.update(
          newBlock().withJump(unresolvedLabel.node, currentBlock, unresolvedLabel.nextBlock));
    }
  }

  public void addGoto(GotoStatementNode gotoNode) {
    NameReferenceNode labelNode = gotoNode.getNameReference();
    String label = labelNode.getImage();
    if (labelTargets.containsKey(label)) {
      nextBlock(newBlock().withJump(gotoNode, labelTargets.get(label), currentBlock));
      return;
    }

    // When labels are used before they are processed they become `unresolved`
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
    LinkedHashMap<Type, BuilderBlock> catches = new LinkedHashMap<>();
    BuilderBlock elseBlock = null;
  }

  public void pushTryContext(List<Entry<Type, BuilderBlock>> catches, BuilderBlock elseBlock) {
    TryContext tryContext = new TryContext();
    tryContext.catches = new LinkedHashMap<>();
    catches.forEach(entry -> tryContext.catches.put(entry.getKey(), entry.getValue()));
    tryContext.elseBlock = elseBlock;
    tryContexts.push(tryContext);
  }

  public boolean inTryContext() {
    return !tryContexts.isEmpty();
  }

  public BuilderBlock getCatchTarget(Type exceptionType) {
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

  public Set<BuilderBlock> getAllCatchTargets() {
    if (tryContexts.isEmpty()) {
      return Collections.emptySet();
    }
    TryContext context = tryContexts.peek();
    Stream<BuilderBlock> elseOrExit =
        Stream.of(Optional.ofNullable(context.elseBlock).orElse(getExitBlock()));
    return Stream.concat(context.catches.values().stream(), elseOrExit)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  public void popTryContext() {
    tryContexts.pop();
  }

  public BuilderBlock getCurrentBlock() {
    return currentBlock;
  }

  public void setCurrentBlock(BuilderBlock currentBlock) {
    this.currentBlock = currentBlock;
  }

  public void addElement(DelphiNode element) {
    currentBlock.addElement(element);
  }

  public BuilderBlock nextBlockToCurrent() {
    return nextBlockTo(currentBlock);
  }

  public BuilderBlock nextBlockTo(BuilderBlock successor) {
    return nextBlock(newBlock().withSuccessor(successor));
  }

  public BuilderBlock nextBlock(BlockBuilder builder) {
    BuilderBlock result = builder.build();
    blocks.add(result);
    currentBlock = result;
    return result;
  }
}
