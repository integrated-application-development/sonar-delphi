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

import au.com.integradev.delphi.antlr.ast.node.RoutineImplementationNodeImpl;
import au.com.integradev.delphi.cfg.ControlFlowGraphFactory;
import au.com.integradev.delphi.cfg.api.Block;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import au.com.integradev.delphi.cfg.api.Finally;
import au.com.integradev.delphi.cfg.api.Linear;
import au.com.integradev.delphi.cfg.api.UnconditionalJump;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.AnonymousMethodNode;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;

@Rule(key = "RedundantJump")
public class RedundantJumpCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this redundant jump.";

  @Override
  public DelphiCheckContext visit(RoutineImplementationNode routine, DelphiCheckContext context) {
    ControlFlowGraph cfg = ((RoutineImplementationNodeImpl) routine).getControlFlowGraph();
    if (cfg != null) {
      cfg.getBlocks().forEach(block -> checkBlock(block, context));
    }

    return super.visit(routine, context);
  }

  @Override
  public DelphiCheckContext visit(AnonymousMethodNode routine, DelphiCheckContext context) {
    CompoundStatementNode compoundStatementNode =
        routine.getFirstChildOfType(CompoundStatementNode.class);
    if (compoundStatementNode != null) {
      ControlFlowGraph cfg = ControlFlowGraphFactory.create(compoundStatementNode);
      cfg.getBlocks().forEach(block -> checkBlock(block, context));
    }

    return super.visit(routine, context);
  }

  private void checkBlock(Block block, DelphiCheckContext context) {
    if (!(block instanceof UnconditionalJump)) {
      return;
    }

    UnconditionalJump jump = (UnconditionalJump) block;
    Block successorWithoutJump = jump.getSuccessorIfRemoved();
    DelphiNode terminator = jump.getTerminator();

    RoutineNameDeclaration routineNameDeclaration = getRoutineNameDeclaration(terminator);
    if (!isContinueOrExit(routineNameDeclaration)
        || isExitWithExpression(routineNameDeclaration, terminator)) {
      return;
    }

    Block successor = jump.getSuccessor();
    successorWithoutJump = nonEmptySuccessor(successorWithoutJump);

    if (!successorWithoutJump.equals(successor)) {
      return;
    }

    Block finallyBlock = getFinallyBlock(block);
    if (finallyBlock != null) {
      if (onlyFinallyBlocksBeforeEnd(finallyBlock)) {
        reportIssue(context, terminator, MESSAGE);
      }
      return;
    }

    reportIssue(context, terminator, MESSAGE);
  }

  private static Block getFinallyBlock(Block block) {
    return block.getSuccessors().stream()
        .filter(Finally.class::isInstance)
        .findFirst()
        .orElse(null);
  }

  private static boolean onlyFinallyBlocksBeforeEnd(Block finallyBlock) {
    while (finallyBlock.getSuccessors().size() == 1) {
      Block finallySuccessor = finallyBlock.getSuccessors().iterator().next();
      if (!(finallySuccessor instanceof Finally)) {
        break;
      }
      finallyBlock = finallySuccessor;
    }
    return finallyBlock.getSuccessors().size() == 1
        && finallyBlock.getSuccessors().iterator().next().getSuccessors().isEmpty();
  }

  private static RoutineNameDeclaration getRoutineNameDeclaration(DelphiNode node) {
    if (!(node instanceof NameReferenceNode)) {
      return null;
    }
    NameDeclaration nameDeclaration = ((NameReferenceNode) node).getNameDeclaration();
    if (!(nameDeclaration instanceof RoutineNameDeclaration)) {
      return null;
    }
    return (RoutineNameDeclaration) nameDeclaration;
  }

  private static boolean isContinueOrExit(RoutineNameDeclaration routineNameDeclaration) {
    if (routineNameDeclaration == null) {
      return false;
    }
    String fullyQualifiedName = routineNameDeclaration.fullyQualifiedName();
    return fullyQualifiedName.equals("System.Continue") || fullyQualifiedName.equals("System.Exit");
  }

  private static boolean isExitWithExpression(
      RoutineNameDeclaration routineNameDeclaration, DelphiNode statement) {
    if (routineNameDeclaration == null) {
      return false;
    }
    String fullyQualifiedName = routineNameDeclaration.fullyQualifiedName();
    if (!fullyQualifiedName.equals("System.Exit")) {
      return false;
    }
    ArgumentListNode argumentList =
        statement.getParent().getFirstChildOfType(ArgumentListNode.class);
    if (argumentList == null) {
      return false;
    }
    return argumentList.getArgumentNodes().size() == 1;
  }

  private static Block nonEmptySuccessor(Block initialBlock) {
    Block result = initialBlock;
    while (result.getElements().isEmpty() && result instanceof Linear) {
      result = ((Linear) result).getSuccessor();
    }
    return result;
  }
}
