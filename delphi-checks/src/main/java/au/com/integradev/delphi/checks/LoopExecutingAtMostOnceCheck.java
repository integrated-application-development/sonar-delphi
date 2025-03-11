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
package au.com.integradev.delphi.checks;

import au.com.integradev.delphi.cfg.ControlFlowGraphFactory;
import au.com.integradev.delphi.cfg.api.Block;
import au.com.integradev.delphi.cfg.api.Branch;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import au.com.integradev.delphi.cfg.api.Terminated;
import au.com.integradev.delphi.utils.ControlFlowGraphUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import org.sonar.check.Rule;
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
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext.Location;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;

@Rule(key = "LoopExecutingAtMostOnce")
public class LoopExecutingAtMostOnceCheck extends DelphiCheck {
  private static final Set<String> EXIT_METHODS =
      Set.of("System.Exit", "System.Break", "System.Halt");

  private final Deque<DelphiNode> loopStack = new ArrayDeque<>();
  private final Deque<List<Location>> violations = new ArrayDeque<>();

  // Loops

  private void pushLoop(DelphiNode node) {
    loopStack.push(node);
    violations.push(new ArrayList<>());
  }

  private void popLoop(DelphiCheckContext context) {
    DelphiNode loop = loopStack.pop();
    List<Location> loopViolations = violations.pop();
    if (loop == null || loopViolations == null || loopViolations.isEmpty()) {
      return;
    }

    context
        .newIssue()
        .onFilePosition(FilePosition.from(loop.getFirstToken()))
        .withMessage("Remove this loop that executes only once.")
        .withSecondaries(loopViolations)
        .report();
  }

  @Override
  public DelphiCheckContext visit(ForStatementNode node, DelphiCheckContext data) {
    pushLoop(node);
    DelphiCheckContext result = super.visit(node, data);
    popLoop(data);
    return result;
  }

  @Override
  public DelphiCheckContext visit(RepeatStatementNode node, DelphiCheckContext data) {
    pushLoop(node);
    DelphiCheckContext result = super.visit(node, data);
    popLoop(data);
    return result;
  }

  @Override
  public DelphiCheckContext visit(WhileStatementNode node, DelphiCheckContext data) {
    pushLoop(node);
    DelphiCheckContext result = super.visit(node, data);
    popLoop(data);
    return result;
  }

  // Statements

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

    return visitExitingNode(node, context, declaration.getImage());
  }

  private DelphiCheckContext visitExitingNode(
      DelphiNode exitingNode, DelphiCheckContext context, String description) {

    if (isInViolatingLoop(exitingNode) && isUnconditionalJump(exitingNode)) {
      List<Location> violationLocations = violations.peek();
      if (violationLocations == null) {
        return context;
      }
      violationLocations.add(
          new Location(
              String.format("Remove this \"%s\" statement or make it conditional.", description),
              exitingNode));
    }
    return context;
  }

  private boolean isInViolatingLoop(DelphiNode jump) {
    DelphiNode loop = this.loopStack.peek();
    if (loop == null) {
      return false;
    }
    ControlFlowGraph cfg = getCFG(loop);
    Block loopBlock =
        getTerminatorBlock(cfg, loop)
            .orElseThrow(
                () -> new IllegalStateException("CFG necessarily contains the loop block"));

    return !hasPredecessorInBlock(loopBlock, loop) && !jumpsBeforeLoop(cfg, loopBlock, jump);
  }

  private static boolean isUnconditionalJump(DelphiNode node) {
    DelphiNode lastStatement = node;
    for (StatementNode statement : node.getParentsOfType(StatementNode.class)) {
      if (statement instanceof ForStatementNode
          || statement instanceof RepeatStatementNode
          || statement instanceof WhileStatementNode) {
        // Reached the loop, it is a non-conditional statement or in a chain of `else` blocks
        return true;
      }

      if (statement instanceof IfStatementNode
          && ((IfStatementNode) statement).getElseStatement() != lastStatement) {
        // If we are in the `if then` branch, then it is not relevant
        return false;
      }

      lastStatement = statement;
    }
    return false;
  }

  private static Optional<Block> getTerminatorBlock(ControlFlowGraph cfg, DelphiNode terminator) {
    return cfg.getBlocks().stream()
        .filter(Terminated.class::isInstance)
        .filter(terminated -> terminator.equals(((Terminated) terminated).getTerminator()))
        .findFirst();
  }

  private static boolean hasPredecessorInBlock(Block block, DelphiNode loop) {
    for (Block predecessor : block.getPredecessors()) {
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

  private static boolean jumpsBeforeLoop(ControlFlowGraph cfg, Block loopBlock, DelphiNode node) {
    if (!(node instanceof GotoStatementNode)) {
      // If the node isn't a `goto`, it cannot jump before the loop
      return false;
    }
    Optional<Block> jumpBlock = getTerminatorBlock(cfg, node);
    if (jumpBlock.isEmpty()) {
      // Unable to find a block whose terminator is the `goto`
      return false;
    }
    Block jumpTarget = jumpBlock.get().getSuccessors().iterator().next();
    if (jumpTarget == null) {
      // There are no successors to the jump block
      return false;
    }
    if (loopBlock instanceof Branch) {
      Branch loopBranch = (Branch) loopBlock;
      if (loopBranch.getTerminator() instanceof RepeatStatementNode
          && loopBranch.getFalseBlock().equals(jumpTarget)) {
        // If the jump target is the start of a `repeat` loop, it is before the loop
        return true;
      }
    }

    // From the jump target, recursively search the successors to find the loop block. Whether the
    // loop block is found relates to if the jump is to before the loop.
    Set<Block> visited = new HashSet<>();
    Queue<Block> queue = new ArrayDeque<>();
    queue.add(jumpTarget);
    while (!queue.isEmpty()) {
      Block search = queue.poll();
      if (search.equals(loopBlock)) {
        return true;
      }
      if ((search.getSuccessors().size() == 1 && search.getSuccessors().contains(jumpTarget))
          || search.equals(cfg.getExitBlock())) {
        return false;
      }

      visited.add(search);
      search.getSuccessors().stream().filter(b -> !visited.contains(b)).forEach(queue::add);
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

  private static ControlFlowGraph getCFG(DelphiNode loop) {
    ControlFlowGraph cfg = ControlFlowGraphUtils.findContainingCFG(loop);
    if (cfg == null) {
      return ControlFlowGraphFactory.create(loop.findChildrenOfType(StatementNode.class));
    }
    return cfg;
  }
}
