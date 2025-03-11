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

import au.com.integradev.delphi.cfg.api.Block;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import au.com.integradev.delphi.cfg.api.Finally;
import au.com.integradev.delphi.cfg.api.Terminated;
import au.com.integradev.delphi.cfg.api.UnconditionalJump;
import au.com.integradev.delphi.utils.ControlFlowGraphUtils;
import java.util.ArrayDeque;
import java.util.Deque;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.FinallyBlockNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.TryStatementNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;

@Rule(key = "RedundantJump")
public class RedundantJumpCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this redundant jump.";

  private final Deque<TryStatementNode> tryFinallyStatements = new ArrayDeque<>();

  @Override
  public DelphiCheckContext visit(TryStatementNode node, DelphiCheckContext data) {
    boolean finallyBlock = node.hasFinallyBlock();
    if (!finallyBlock) {
      return super.visit(node, data);
    }
    this.tryFinallyStatements.push(node);
    super.visit(node.getStatementList(), data);
    this.tryFinallyStatements.pop();
    return super.visit(node.getFinallyBlock(), data);
  }

  @Override
  public DelphiCheckContext visit(NameReferenceNode node, DelphiCheckContext data) {
    RoutineNameDeclaration routineNameDeclaration = getRoutineNameDeclaration(node);
    if (routineNameDeclaration != null
        && (isContinue(routineNameDeclaration)
            || isExitWithoutArgs(routineNameDeclaration, node))) {
      checkJumpNode(node, data);
      return data;
    }
    return super.visit(node, data);
  }

  private static RoutineNameDeclaration getRoutineNameDeclaration(NameReferenceNode node) {
    NameDeclaration nameDeclaration = node.getNameDeclaration();
    if (nameDeclaration instanceof RoutineNameDeclaration) {
      return (RoutineNameDeclaration) nameDeclaration;
    }
    return null;
  }

  private static boolean isContinue(RoutineNameDeclaration node) {
    String name = node.fullyQualifiedName();
    return name.equals("System.Continue");
  }

  private static boolean isExitWithoutArgs(RoutineNameDeclaration node, DelphiNode statement) {
    String name = node.fullyQualifiedName();
    if (!name.equals("System.Exit")) {
      return false;
    }

    var argumentList = statement.getParent().getFirstChildOfType(ArgumentListNode.class);
    int arguments = argumentList == null ? 0 : argumentList.getArgumentNodes().size();

    return arguments == 0;
  }

  private static Block findBlockWithTerminator(ControlFlowGraph cfg, DelphiNode node) {
    for (Block block : cfg.getBlocks()) {
      if ((block instanceof Terminated) && ((Terminated) block).getTerminator() == node) {
        return block;
      }
    }
    return null;
  }

  private void checkJumpNode(NameReferenceNode node, DelphiCheckContext context) {
    ControlFlowGraph cfg = ControlFlowGraphUtils.findContainingCFG(node);
    if (cfg == null) {
      return;
    }

    Block terminatedBlock = findBlockWithTerminator(cfg, node);
    if (!(terminatedBlock instanceof UnconditionalJump)) {
      // can't be a redundant jump without a jump
      return;
    }

    UnconditionalJump jump = (UnconditionalJump) terminatedBlock;
    Block successor = jump.getSuccessor();
    if (!successor.equals(jump.getSuccessorIfRemoved())) {
      // without the jump, the successor block would be different
      return;
    }

    if (isViolation(cfg)) {
      reportIssue(context, jump.getTerminator(), MESSAGE);
    }
  }

  private boolean isViolation(ControlFlowGraph cfg) {
    for (TryStatementNode tryFinally : this.tryFinallyStatements) {
      Finally finallyBlock = findFinallyBlock(cfg, tryFinally.getFinallyBlock());
      if (finallyBlock == null) {
        // if the finally block cannot be found, we have traversed outside the scope of the cfg
        break;
      }
      if (!finallyBlock.getSuccessor().equals(finallyBlock.getExceptionSuccessor())) {
        // multiple paths after the finally corresponds to code that would be skipped from the jump
        return false;
      }
    }
    // if no invalidating try-finally blocks are found, the use is a violation
    return true;
  }

  private static Finally findFinallyBlock(ControlFlowGraph cfg, FinallyBlockNode element) {
    if (element == null) {
      return null;
    }
    for (Block block : cfg.getBlocks()) {
      if ((block instanceof Finally) && ((Finally) block).getTerminator().equals(element)) {
        return (Finally) block;
      }
    }
    return null;
  }
}
