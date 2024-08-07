/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.checks;

import au.com.integradev.delphi.antlr.ast.node.AnonymousMethodNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.RoutineImplementationNodeImpl;
import au.com.integradev.delphi.cfg.ControlFlowGraphFactory;
import au.com.integradev.delphi.cfg.api.Block;
import au.com.integradev.delphi.cfg.api.Branch;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import au.com.integradev.delphi.cfg.api.Terminated;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Supplier;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ForInStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ForStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ForToStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.GotoStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.IfStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.RaiseStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.RepeatStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.ast.WhileStatementNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;

@Rule(key = "LoopExecutingAtMostOnce")
public class LoopExecutingAtMostOnceCheck extends DelphiCheck {
  private static final Set<String> EXIT_METHODS =
      Set.of("System.Exit", "System.Break", "System.Halt");

  @Override
  public DelphiCheckContext visit(RaiseStatementNode node, DelphiCheckContext context) {
    return visitExitingNode(node, context, "raise");
  }

  @Override
  public DelphiCheckContext visit(GotoStatementNode node, DelphiCheckContext context) {
    return visitExitingNode(node, context, "goto");
  }

  @Override
  public DelphiCheckContext visit(NameReferenceNode node, DelphiCheckContext context) {
    NameDeclaration declaration = node.getLastName().getNameDeclaration();
    if (!(declaration instanceof RoutineNameDeclaration)) {
      return context;
    }
    String fullyQualifiedName = ((RoutineNameDeclaration) declaration).fullyQualifiedName();
    if (!EXIT_METHODS.contains(fullyQualifiedName)) {
      return context;
    }

    return visitExitingNode(node, context, fullyQualifiedName);
  }

  private DelphiCheckContext visitExitingNode(
      DelphiNode node, DelphiCheckContext context, String description) {
    DelphiNode enclosingStatement = findEnclosingStatement(node);
    DelphiNode enclosingLoop = findEnclosingLoop(node);
    if (enclosingStatement == null || enclosingLoop == null) {
      return context;
    }

    if (isViolation(enclosingLoop, node) && violationInRelevantStatement(enclosingStatement)) {
      reportIssue(
          context,
          node,
          String.format("Remove this \"%s\" statement or make it conditional.", description));
    }
    return context;
  }

  private static boolean isLoopNode(DelphiNode node) {
    return node instanceof RepeatStatementNode
        || node instanceof ForStatementNode
        || node instanceof WhileStatementNode;
  }

  private static DelphiNode findEnclosingStatement(DelphiNode node) {
    DelphiNode parent = node;
    if (!(parent instanceof StatementNode)) {
      // Finding the enclosing statement of the NameReferenceNode
      parent = parent.getFirstParentOfType(StatementNode.class);
    }
    if (parent == null) {
      return null;
    }
    do {
      parent = parent.getFirstParentOfType(StatementNode.class);
    } while (parent instanceof CompoundStatementNode);
    return parent;
  }

  private static DelphiNode findEnclosingLoop(DelphiNode node) {
    DelphiNode parent = node;
    while (parent != null && !isLoopNode(parent)) {
      parent = parent.getFirstParentOfType(StatementNode.class);
    }
    return parent;
  }

  private static boolean isViolation(DelphiNode loop, DelphiNode jump) {
    ControlFlowGraph cfg = getCFG(loop);
    Block loopBlock =
        getTerminatorBlock(cfg, loop)
            .orElseThrow(
                () -> new IllegalStateException("CFG necessarily contains the loop block"));

    return !hasPredecessorInBlock(loopBlock, loop) && !jumpsBeforeLoop(cfg, loopBlock, jump);
  }

  private static boolean violationInRelevantStatement(DelphiNode enclosingStatement) {
    if (!(enclosingStatement instanceof IfStatementNode)) {
      return true;
    }

    IfStatementNode ifStatement = (IfStatementNode) enclosingStatement;
    if (!ifStatement.hasElseBranch()) {
      return false;
    }
    return !(ifStatement.getElseStatement() instanceof IfStatementNode);
  }

  private static Optional<Block> getTerminatorBlock(ControlFlowGraph cfg, DelphiNode loop) {
    return cfg.getBlocks().stream()
        .filter(block -> block.getSuccessors() instanceof Terminated)
        .filter(
            terminated -> loop.equals(((Terminated) terminated.getSuccessors()).getTerminator()))
        .findFirst();
  }

  private static boolean hasPredecessorInBlock(Block block, DelphiNode loop) {
    for (Block predecessor : block.getPredecessorBlocks()) {
      List<DelphiNode> predecessorElements = predecessor.getElements();
      if (predecessorElements.isEmpty()) {
        return hasPredecessorInBlock(predecessor, loop);
      }
      DelphiNode predecessorFirstElement = predecessorElements.get(0);

      if (isForStatementInitializer(predecessorFirstElement, loop)) {
        continue;
      }

      if (isDescendant(predecessorFirstElement, loop)) {
        return true;
      }
    }

    return false;
  }

  private static boolean jumpsBeforeLoop(ControlFlowGraph cfg, Block loopBlock, DelphiNode jump) {
    if (!(jump instanceof GotoStatementNode)) {
      return false;
    }
    Optional<Block> jumpBlock = getTerminatorBlock(cfg, jump);
    if (jumpBlock.isEmpty()) {
      return false;
    }
    Block jumpTarget = jumpBlock.get().getSuccessorBlocks().iterator().next();
    if (jumpTarget == null) {
      return false;
    }
    if (loopBlock.getSuccessors() instanceof Branch) {
      Branch loopBranch = (Branch) loopBlock.getSuccessors();
      if (loopBranch.getTerminator() instanceof RepeatStatementNode
          && loopBranch.getFalseBlock().equals(jumpTarget)) {
        return true;
      }
    }

    Set<Block> visited = new HashSet<>();
    PriorityQueue<Block> queue =
        new PriorityQueue<>((a, b) -> Math.abs(a.getId() - b.getId() - loopBlock.getId()));
    queue.add(jumpTarget);
    while (!queue.isEmpty()) {
      Block search = queue.poll();
      if (search.equals(loopBlock)) {
        return true;
      }
      if (search.getSuccessorBlocks().size() == 1
          && search.getSuccessorBlocks().contains(jumpTarget)) {
        return false;
      }

      if (search.equals(cfg.getExitBlock())) {
        return false;
      }
      visited.add(search);
      search.getSuccessorBlocks().stream().filter(b -> !visited.contains(b)).forEach(queue::add);
    }

    return false;
  }

  private static boolean isForStatementInitializer(DelphiNode lastElement, DelphiNode loop) {
    if (loop instanceof ForToStatementNode) {
      return isDescendant(lastElement, ((ForToStatementNode) loop).getInitializerExpression())
          || isDescendant(lastElement, ((ForToStatementNode) loop).getTargetExpression());
    }
    return loop instanceof ForInStatementNode
        && isDescendant(lastElement, ((ForInStatementNode) loop).getEnumerable());
  }

  private static boolean isDescendant(DelphiNode descendant, DelphiNode target) {
    DelphiNode parent = descendant;
    while (parent != null) {
      if (parent.equals(target)) {
        return true;
      }
      parent = parent.getParent();
    }
    return false;
  }

  private static Supplier<ControlFlowGraph> getCFGSupplier(DelphiNode node) {
    if (node instanceof RoutineImplementationNodeImpl) {
      return ((RoutineImplementationNodeImpl) node)::getControlFlowGraph;
    }
    if (node instanceof AnonymousMethodNodeImpl) {
      return ((AnonymousMethodNodeImpl) node)::getControlFlowGraph;
    }
    return null;
  }

  private static ControlFlowGraph getCFG(DelphiNode loop) {
    DelphiNode parent = loop.getParent();
    Supplier<ControlFlowGraph> cfgSupplier = getCFGSupplier(parent);
    while (parent != null && cfgSupplier == null) {
      parent = parent.getParent();
      cfgSupplier = getCFGSupplier(parent);
    }
    if (cfgSupplier != null) {
      return cfgSupplier.get();
    }
    return ControlFlowGraphFactory.create(loop.findChildrenOfType(StatementNode.class));
  }
}
