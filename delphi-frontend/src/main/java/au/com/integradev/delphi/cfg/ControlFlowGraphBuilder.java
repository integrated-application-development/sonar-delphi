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
import au.com.integradev.delphi.cfg.block.BlockImpl;
import au.com.integradev.delphi.cfg.block.ProtoBlock;
import au.com.integradev.delphi.cfg.block.ProtoBlockFactory;
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
  private final List<ProtoBlock> blocks = new ArrayList<>();
  private final Deque<ProtoBlock> exitBlocks = new ArrayDeque<>();
  private final Deque<ProtoBlock> breakTargets = new ArrayDeque<>();
  private final Deque<ProtoBlock> continueTargets = new ArrayDeque<>();
  private final Map<String, ProtoBlock> labelTargets = new HashMap<>();
  private final Map<String, List<UnresolvedLabel>> unresolvedLabels = new HashMap<>();
  private final Deque<TryContext> tryContexts = new ArrayDeque<>();

  private ProtoBlock currentBlock;

  public ControlFlowGraphBuilder() {
    ProtoBlock exitBlock = ProtoBlockFactory.exitBlock();
    addBlock(exitBlock);
    exitBlocks.add(exitBlock);
    addBlockBefore(exitBlock);
  }

  public ControlFlowGraph build() {
    Map<ProtoBlock, Block> map = new LinkedHashMap<>();
    for (ProtoBlock block : blocks) {
      map.put(block, block.createBlock());
    }
    for (ProtoBlock block : blocks) {
      block.updateBlockData(map);
    }

    ControlFlowGraphImpl cfg =
        new ControlFlowGraphImpl(
            map.get(currentBlock), map.get(exitBlocks.peek()), new ArrayList<>(map.values()));
    cfg.prune();

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

  public ProtoBlock getExitBlock() {
    return exitBlocks.peek();
  }

  public ProtoBlock getBreakTarget() {
    return breakTargets.peek();
  }

  public ProtoBlock getContinueTarget() {
    return continueTargets.peek();
  }

  public void pushLoopContext(ProtoBlock continueTarget, ProtoBlock breakTarget) {
    continueTargets.push(continueTarget);
    breakTargets.push(breakTarget);
  }

  public void popLoopContext() {
    breakTargets.pop();
    continueTargets.pop();
  }

  public void pushExitBlock(ProtoBlock target) {
    exitBlocks.push(target);
  }

  public void popExitBlock() {
    exitBlocks.pop();
  }

  private static class UnresolvedLabel {
    ProtoBlock nextBlock;
    ProtoBlock block;
    DelphiNode node;
  }

  public void addLabel(LabelStatementNode labelNode) {
    NameReferenceNode labelName = labelNode.getNameReference();
    String label = labelName.getImage();

    labelTargets.put(label, currentBlock);
    if (!unresolvedLabels.containsKey(label)) {
      return;
    }

    // When "resolving" label, all the previously unresolved targets must be updated
    for (UnresolvedLabel unresolvedLabel : unresolvedLabels.get(label)) {
      unresolvedLabel.block.update(
          ProtoBlockFactory.jump(unresolvedLabel.node, currentBlock, unresolvedLabel.nextBlock));
    }
  }

  public void addGoto(GotoStatementNode gotoNode) {
    NameReferenceNode labelNode = gotoNode.getNameReference();
    String label = labelNode.getImage();
    if (labelTargets.containsKey(label)) {
      addBlock(ProtoBlockFactory.jump(gotoNode, labelTargets.get(label), currentBlock));
      return;
    }

    // When labels are used before they are processed they become `unresolved`
    addBlockBeforeCurrent();
    unresolvedLabels.putIfAbsent(label, new ArrayList<>());
    UnresolvedLabel unresolvedLabel = new UnresolvedLabel();
    unresolvedLabel.nextBlock = currentBlock;
    unresolvedLabel.block = addBlockBeforeCurrent();
    unresolvedLabel.node = gotoNode;
    unresolvedLabels.get(label).add(unresolvedLabel);

    addElement(labelNode);
  }

  private static class TryContext {
    LinkedHashMap<Type, ProtoBlock> catches = new LinkedHashMap<>();
    ProtoBlock elseBlock;
  }

  public void pushTryFinallyContext() {
    tryContexts.push(new TryContext());
  }

  public void pushTryExceptContext(List<Entry<Type, ProtoBlock>> catches, ProtoBlock elseBlock) {
    TryContext tryContext = new TryContext();
    tryContext.catches = new LinkedHashMap<>();
    catches.forEach(entry -> tryContext.catches.put(entry.getKey(), entry.getValue()));
    tryContext.elseBlock = elseBlock;
    tryContexts.push(tryContext);
  }

  public boolean inTryContext() {
    return !tryContexts.isEmpty();
  }

  public ProtoBlock getCatchTarget(Type exceptionType) {
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

  private static boolean isCompatibleType(Type exceptionType, Type catchType) {
    return exceptionType.is(catchType) || exceptionType.isDescendantOf(catchType);
  }

  public Set<ProtoBlock> getAllCatchTargets() {
    if (tryContexts.isEmpty()) {
      return Collections.emptySet();
    }
    TryContext context = tryContexts.peek();
    Stream<ProtoBlock> elseOrExit =
        Stream.of(Optional.ofNullable(context.elseBlock).orElse(getExitBlock()));
    return Stream.concat(context.catches.values().stream(), elseOrExit)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  public void popTryContext() {
    tryContexts.pop();
  }

  public ProtoBlock getCurrentBlock() {
    return currentBlock;
  }

  public void setCurrentBlock(ProtoBlock currentBlock) {
    this.currentBlock = currentBlock;
  }

  public void addElement(DelphiNode element) {
    currentBlock.addElement(element);
  }

  public ProtoBlock addBlockBeforeCurrent() {
    addBlockBefore(currentBlock);
    return currentBlock;
  }

  public void addBlockBefore(ProtoBlock successor) {
    addBlock(ProtoBlockFactory.linear(successor));
  }

  public void addBlock(ProtoBlock block) {
    blocks.add(block);
    currentBlock = block;
  }
}
