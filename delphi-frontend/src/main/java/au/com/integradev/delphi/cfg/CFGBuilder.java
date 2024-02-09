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

import au.com.integradev.delphi.cfg.ControlFlowGraphData.TryStatement;
import au.com.integradev.delphi.cfg.ControlFlowGraphImpl.Block;
import au.com.integradev.delphi.cfg.ControlFlowGraphImpl.TerminatorKind;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.sonar.plugins.communitydelphi.api.ast.AnonymousMethodNode;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentNode;
import org.sonar.plugins.communitydelphi.api.ast.ArrayConstructorNode;
import org.sonar.plugins.communitydelphi.api.ast.AsmStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.CaseStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.CommonDelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ConstStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExceptBlockNode;
import org.sonar.plugins.communitydelphi.api.ast.ExceptItemNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.FinallyBlockNode;
import org.sonar.plugins.communitydelphi.api.ast.ForInStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ForLoopVarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.ForLoopVarReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.ForToStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.GotoStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.IfStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.IntegerLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.LabelStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationListNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.NilLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.ParenthesizedExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.RaiseStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.RangeExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.RealLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.RepeatStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.SimpleNameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementListNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.ast.TextLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.TryStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.UnaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.VarStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.WhileStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.WithStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.utils.ExpressionNodeUtils;
import org.sonar.plugins.communitydelphi.api.operator.BinaryOperator;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonarsource.analyzer.commons.collections.ListUtils;

public class CFGBuilder {
  private CFGBuilder() {
    // Utility class
  }

  public static void build(List<? extends DelphiNode> nodes, ControlFlowGraphData data) {
    ListUtils.reverse(nodes).forEach(node -> build(node, data));
  }

  public static void build(DelphiNode node, ControlFlowGraphData data) {
    // Literals and references
    if (node instanceof IntegerLiteralNode
        || node instanceof RealLiteralNode
        || node instanceof TextLiteralNode
        || node instanceof NilLiteralNode
        || node instanceof SimpleNameDeclarationNode) {
      data.currentBlock.addElement(node);
    } else if (node instanceof NameReferenceNode) {
      buildNameReference((NameReferenceNode) node, data);
    } else if (node instanceof RangeExpressionNode) {
      build(((RangeExpressionNode) node).getLowExpression(), data);
      build(((RangeExpressionNode) node).getHighExpression(), data);
    } else if (node instanceof ArrayConstructorNode) {
      build(((ArrayConstructorNode) node).getElements(), data);
    } else if (node instanceof NameDeclarationListNode) {
      build(((NameDeclarationListNode) node).getDeclarations(), data);
    } else if (node instanceof ParenthesizedExpressionNode) {
      build(((ParenthesizedExpressionNode) node).getExpression(), data);
    } else if (node instanceof PrimaryExpressionNode) {
      build(node.getChild(0), data);
    } else if (node instanceof BinaryExpressionNode) {
      buildBinaryExpression((BinaryExpressionNode) node, data);
    } else if (node instanceof UnaryExpressionNode) {
      buildUnaryExpression((UnaryExpressionNode) node, data);
    } else if (node instanceof ForLoopVarDeclarationNode) {
      build(((ForLoopVarDeclarationNode) node).getNameDeclarationNode(), data);
    } else if (node instanceof ForLoopVarReferenceNode) {
      build(((ForLoopVarReferenceNode) node).getNameReference(), data);
    } else if (node instanceof ArgumentNode) {
      build(((ArgumentNode) node).getExpression(), data);
    } else if (node instanceof CommonDelphiNode) {
      if (node.getParent() instanceof ExpressionNode
          && ExpressionNodeUtils.isInherited((ExpressionNode) node.getParent())) {
        // arguments
        ArgumentListNode argumentList =
            node.getParent().getFirstChildOfType(ArgumentListNode.class);
        if (argumentList != null) {
          build(argumentList.getArgumentNodes(), data);
        }
        // name
        build(node.getParent().getFirstChildOfType(NameReferenceNode.class), data);
        // inherited
        data.currentBlock.addElement(node);
      }
    } else if (node instanceof AnonymousMethodNode) {
      // Ignore, they can have their own CFG
    }
    // Statements
    else if (node instanceof StatementListNode) {
      build(((StatementListNode) node).getStatements(), data);
    } else if (node instanceof IfStatementNode) {
      buildIfStatement((IfStatementNode) node, data);
    } else if (node instanceof VarStatementNode) {
      build(((VarStatementNode) node).getNameDeclarationList(), data);
      build(((VarStatementNode) node).getExpression(), data);
    } else if (node instanceof ConstStatementNode) {
      build(((ConstStatementNode) node).getNameDeclarationNode(), data);
      build(((ConstStatementNode) node).getExpression(), data);
    } else if (node instanceof CaseStatementNode) {
      buildCaseStatement((CaseStatementNode) node, data);
    } else if (node instanceof RepeatStatementNode) {
      buildRepeatStatement((RepeatStatementNode) node, data);
    } else if (node instanceof WhileStatementNode) {
      buildWhileStatement((WhileStatementNode) node, data);
    } else if (node instanceof ForToStatementNode) {
      buildForToStatement((ForToStatementNode) node, data);
    } else if (node instanceof ForInStatementNode) {
      buildForInStatement((ForInStatementNode) node, data);
    } else if (node instanceof WithStatementNode) {
      buildWithStatement((WithStatementNode) node, data);
    } else if (node instanceof TryStatementNode) {
      buildTryStatement((TryStatementNode) node, data);
    } else if (node instanceof RaiseStatementNode) {
      RaiseStatementNode raiseNode = (RaiseStatementNode) node;
      ExpressionNode raiseExpression = ((RaiseStatementNode) node).getRaiseExpression();
      buildRaiseStatement(
          raiseNode,
          type ->
              raiseExpression != null
                  && (raiseExpression.getType().isDescendantOf(type)
                      || raiseExpression.getType().is(type)),
          data);
      if (raiseExpression == null) {
        data.currentBlock.addElement(node);
      } else {
        build(raiseExpression, data);
      }
    } else if (node instanceof AsmStatementNode) {
      // Not supported, ignored
    } else if (node instanceof CompoundStatementNode) {
      build(((CompoundStatementNode) node).getStatementList(), data);
    } else if (node instanceof LabelStatementNode) {
      build(node.getFirstChildOfType(StatementNode.class), data);
      buildLabel(node.getChild(0), data);
      data.currentBlock = data.createBlock(data.currentBlock);
    } else if (node instanceof AssignmentStatementNode) {
      build(((AssignmentStatementNode) node).getAssignee(), data);
      build(((AssignmentStatementNode) node).getValue(), data);
    } else if (node instanceof ExpressionStatementNode) {
      build(((ExpressionStatementNode) node).getExpression(), data);
    } else if (node instanceof GotoStatementNode) {
      buildGoto((GotoStatementNode) node, data);
    } else if (node != null) {
      throw new UnsupportedOperationException("Unknown node type " + node.getClass());
    }
  }

  private static void buildNameReference(NameReferenceNode node, ControlFlowGraphData data) {
    if (node.getLastName().getNameDeclaration() instanceof RoutineNameDeclaration) {
      String routineName =
          ((RoutineNameDeclaration) node.getLastName().getNameDeclaration()).fullyQualifiedName();
      switch (routineName) {
        case "System.Exit":
          buildExitStatement(node, data);
          break;
        case "System.Break":
          buildBreakStatement(node, data);
          break;
        case "System.Halt":
          buildSink(node, data);
          break;
        case "System.Continue":
          buildContinueStatement(node, data);
          break;
        default:
          handleExceptionalPaths(data);
          data.currentBlock.addElement(node);
          break;
      }
    } else {
      data.currentBlock.addElement(node);
    }
  }

  private static void buildExitStatement(DelphiNode terminator, ControlFlowGraphData data) {
    data.currentBlock =
        createUnconditionalJump(
            data, TerminatorKind.EXIT, terminator, data.exitBlock(), data.currentBlock);
    DelphiNode sibling = terminator.getParent().getChild(1);
    if (terminator.getChildIndex() == 0 && sibling instanceof ArgumentListNode) {
      build(sibling.getFirstDescendantOfType(ExpressionNode.class), data);
    }
  }

  private static void buildSink(DelphiNode terminator, ControlFlowGraphData data) {
    data.currentBlock =
        createUnconditionalJump(data, TerminatorKind.HALT, terminator, null, data.currentBlock);
    data.currentBlock.setSink(true);

    DelphiNode sibling = terminator.getParent().getChild(1);
    if (terminator.getChildIndex() == 0 && sibling instanceof ArgumentListNode) {
      build(sibling.getFirstDescendantOfType(ExpressionNode.class), data);
    }
  }

  private static void buildBreakStatement(DelphiNode node, ControlFlowGraphData data) {
    if (data.breakTargets.isEmpty()) {
      throw new IllegalStateException("'break' statement not in loop or switch statement");
    }

    Block targetBlock = data.breakTargets.getLast();
    data.currentBlock =
        createUnconditionalJump(data, TerminatorKind.BREAK, node, targetBlock, data.currentBlock);
    if (data.currentBlock.exitBlock() != null) {
      data.currentBlock.setExitBlock(null);
    }
  }

  private static void buildContinueStatement(DelphiNode node, ControlFlowGraphData data) {
    if (data.continueTargets.isEmpty()) {
      throw new IllegalStateException("'continue' statement not in loop or switch statement");
    }

    Block targetBlock = data.continueTargets.getLast();
    data.currentBlock =
        createUnconditionalJump(
            data, TerminatorKind.CONTINUE, node, targetBlock, data.currentBlock);
    data.currentBlock.setExitBlock(null);
  }

  private static void handleExceptionalPaths(ControlFlowGraphData data) {
    TryStatement enclosingTry = data.enclosingTry.peek();
    if (enclosingTry == data.outerTry) {
      return;
    }

    data.currentBlock = data.createBlock(data.currentBlock);
    if (enclosingTry == null || Boolean.TRUE.equals(data.enclosedByCatch.peek())) {
      data.currentBlock.addException(data.exitBlocks.peek());
      return;
    }

    data.currentBlock.addExceptions(enclosingTry.catches.values());
    if (enclosingTry.defaultCatch != null) {
      data.currentBlock.addException(enclosingTry.defaultCatch);
    } else {
      data.currentBlock.addException(data.exitBlocks.peek());
    }
  }

  private static void buildIfStatement(IfStatementNode node, ControlFlowGraphData data) {
    Block next = data.currentBlock;
    // else
    Block elseBlock = next;
    StatementNode elseStatement = node.getElseStatement();
    if (elseStatement != null) {
      if (!(elseStatement instanceof IfStatementNode)) {
        data.currentBlock = data.createBlock(next);
      }
      build(elseStatement, data);
      elseBlock = data.currentBlock;
    }
    // then
    data.currentBlock = data.createBlock(next);
    build(node.getThenStatement(), data);
    Block thenBlock = data.currentBlock;

    // condition
    data.currentBlock = data.createBranch(node, thenBlock, elseBlock);

    buildCondition(data, node.getGuardExpression(), thenBlock, elseBlock);
  }

  private static void buildTryStatement(TryStatementNode node, ControlFlowGraphData data) {
    data.currentBlock = data.createBlock(data.currentBlock);

    FinallyBlockNode finallyBlockNode = node.getFinallyBlock();
    if (finallyBlockNode != null) {
      data.currentBlock.setFinallyBlock(true);
      Block finallyBlock = data.currentBlock;
      build(finallyBlockNode.getStatementList(), data);
      finallyBlock.addExitSuccessor(data.exitBlock());
      data.exitBlocks.push(data.currentBlock);
      addContinueTarget(data, data.currentBlock);
      data.currentBlock.setFinallyBlock(true);
      data.breakTargets.addLast(data.currentBlock);
    }
    Block finallyOrEndBlock = data.currentBlock;
    Block beforeFinally = data.createBlock(finallyOrEndBlock);
    TryStatement tryStatement = new TryStatement();
    data.enclosingTry.push(tryStatement);
    data.enclosedByCatch.push(false);

    if (node.hasExceptBlock()) {
      ExceptBlockNode exceptBlock = node.getExceptBlock();
      if (exceptBlock.isBareExcept()) {
        data.currentBlock = data.createBlock(finallyOrEndBlock);
        addExceptTarget(data, exceptBlock.getStatementList(), null);
        tryStatement.setDefaultCatch(data.currentBlock);
      } else if (exceptBlock.getElseBlock() != null) {
        data.currentBlock = data.createBlock(finallyOrEndBlock);
        addExceptTarget(data, exceptBlock.getElseBlock().getStatementList(), null);
        tryStatement.setDefaultCatch(data.currentBlock);
      }
      if (exceptBlock.hasHandlers()) {
        for (ExceptItemNode exceptItem : ListUtils.reverse(node.getExceptBlock().getHandlers())) {
          data.currentBlock = data.createBlock(finallyOrEndBlock);
          addExceptTarget(data, exceptItem.getStatement(), exceptItem.getExceptionName());
          tryStatement.addCatch(exceptItem.getExceptionType().getType(), data.currentBlock);
        }
      }
    }
    data.currentBlock = beforeFinally;
    build(node.getStatementList(), data);
    data.enclosingTry.pop();
    data.enclosedByCatch.pop();
    data.currentBlock = data.createBlock(data.currentBlock);
    data.currentBlock.addElement(node);
    if (finallyBlockNode != null) {
      data.exitBlocks.pop();
      data.continueTargets.removeLast();
      data.breakTargets.removeLast();
    }
  }

  private static void addExceptTarget(
      ControlFlowGraphData data, DelphiNode statement, NameDeclarationNode exceptionName) {
    data.enclosedByCatch.push(true);
    build(statement, data);
    build(exceptionName, data);
    data.currentBlock.setExceptBlock(true);
    data.enclosedByCatch.pop();
  }

  private static void buildRaiseStatement(
      DelphiNode node, Predicate<Type> catchFilter, ControlFlowGraphData data) {
    Block jumpTo = data.exitBlock();
    TryStatement enclosingTryCatch = data.enclosingTry.peek();

    if (enclosingTryCatch != null) {
      jumpTo = enclosingTryCatch.findCatch(catchFilter).orElse(data.exitBlock());
    }
    data.currentBlock =
        createUnconditionalJump(data, TerminatorKind.RAISE, node, jumpTo, data.currentBlock);
  }

  private static void buildLabel(DelphiNode label, ControlFlowGraphData data) {
    String targetLabel = label.getImage();
    if (data.getLabelBlocks().put(targetLabel, data.currentBlock) != null) {
      throw new IllegalStateException(String.format("label '%s' already declared", targetLabel));
    }
    List<Block> blocksToResolve = data.getUnresolvedLabelTargets().remove(targetLabel);
    if (blocksToResolve != null) {
      for (Block block : blocksToResolve) {
        block.addSuccessor(data.currentBlock);
      }
    }
  }

  private static void buildGoto(GotoStatementNode gotoNode, ControlFlowGraphData data) {
    DelphiNode label = gotoNode.getNameReference();
    String targetLabel = label.getImage();
    Block target = data.getLabelBlocks().get(targetLabel);
    data.currentBlock =
        createUnconditionalJump(data, TerminatorKind.NODE, gotoNode, target, data.currentBlock);
    data.currentBlock.addElement(label);
    if (target == null) {
      data.getUnresolvedLabelTargets().putIfAbsent(targetLabel, new ArrayList<>());
      data.getUnresolvedLabelTargets().get(targetLabel).add(data.currentBlock);
    }
  }

  private static void buildCaseStatement(CaseStatementNode node, ControlFlowGraphData data) {
    Block caseSuccessor = data.currentBlock;
    boolean hasElseBlock = node.getElseBlockNode() != null;

    // condition
    data.currentBlock = data.createBlock();
    data.currentBlock.setTerminator(TerminatorKind.NODE, node);
    Block caseBlock = data.currentBlock;

    List<ExpressionNode> caseLabels =
        node.getCaseItems().stream()
            .map(caseNode -> caseNode.getFirstDescendantOfType(ExpressionNode.class))
            .collect(Collectors.toList());
    build(caseLabels, data);
    build(node.getFirstDescendantOfType(ExpressionNode.class), data);
    Block conditionBlock = data.currentBlock;

    // else
    if (hasElseBlock) {
      data.currentBlock = data.createBlock(caseSuccessor);
      build(node.getElseBlockNode().getStatementList(), data);
      data.currentBlock.setElseBlock(true);
      caseBlock.addSuccessor(data.currentBlock);
    }

    // cases
    List<StatementNode> caseStatements =
        node.getCaseItems().stream()
            .map(caseNode -> caseNode.getFirstDescendantOfType(StatementNode.class))
            .collect(Collectors.toList());
    for (StatementNode statement : ListUtils.reverse(caseStatements)) {
      data.currentBlock = data.createBlock(caseSuccessor);
      build(statement, data);
      caseBlock.addSuccessor(data.currentBlock);
    }

    data.currentBlock = caseBlock;
    if (!hasElseBlock) {
      data.currentBlock.addSuccessor(caseSuccessor);
    }
    data.currentBlock = conditionBlock;
  }

  private static void buildRepeatStatement(RepeatStatementNode node, ControlFlowGraphData data) {
    Block trueBranch = data.currentBlock;
    Block loopback = data.createBlock();

    // condition
    data.currentBlock = data.createBranch(node, trueBranch, loopback);
    buildCondition(data, node.getFirstChildOfType(ExpressionNode.class), trueBranch, loopback);

    // body
    addContinueTarget(data, data.currentBlock);
    data.currentBlock = data.createBlock(data.currentBlock);
    data.breakTargets.addLast(trueBranch);
    build(node.getFirstChildOfType(StatementListNode.class), data);
    data.breakTargets.removeLast();
    data.continueTargets.removeLast();

    loopback.addSuccessor(data.currentBlock);
    data.currentBlock = data.createBlock(data.currentBlock);
  }

  private static void buildWhileStatement(WhileStatementNode node, ControlFlowGraphData data) {
    Block falseBranch = data.currentBlock;
    Block loopback = data.createBlock();

    // body
    data.currentBlock = data.createBlock(loopback);
    addContinueTarget(data, loopback);
    data.breakTargets.addLast(falseBranch);
    build(node.getFirstChildOfType(StatementNode.class), data);
    data.breakTargets.removeLast();
    data.continueTargets.removeLast();
    Block bodyBlock = data.currentBlock;

    // condition
    data.currentBlock = data.createBranch(node, bodyBlock, falseBranch);
    buildCondition(data, node.getFirstChildOfType(ExpressionNode.class), bodyBlock, falseBranch);

    loopback.addSuccessor(data.currentBlock);
    data.currentBlock = data.createBlock(data.currentBlock);
  }

  private static void buildForToStatement(ForToStatementNode node, ControlFlowGraphData data) {
    Block afterLoop = data.currentBlock;
    Block statementBlock = data.createBlock();
    Block loopback = data.createBranch(node, statementBlock, afterLoop);
    data.currentBlock = data.createBlock(loopback);
    addContinueTarget(data, loopback);
    data.breakTargets.addLast(afterLoop);
    build(node.getStatement(), data);
    data.breakTargets.removeLast();
    data.continueTargets.removeLast();
    statementBlock.addSuccessor(data.currentBlock);
    data.currentBlock = loopback;
    build(node.getVariable(), data);
    data.currentBlock = data.createBlock(data.currentBlock);
    build(node.getTargetExpression(), data);
    data.currentBlock = data.createBlock(data.currentBlock);
    build(node.getInitializerExpression(), data);
    data.currentBlock = data.createBlock(data.currentBlock);
  }

  private static void buildForInStatement(ForInStatementNode node, ControlFlowGraphData data) {
    Block afterLoop = data.currentBlock;
    Block statementBlock = data.createBlock();
    Block loopback = data.createBranch(node, statementBlock, afterLoop);
    data.currentBlock = data.createBlock(loopback);
    addContinueTarget(data, loopback);
    data.breakTargets.addLast(afterLoop);
    build(node.getStatement(), data);
    data.breakTargets.removeLast();
    data.continueTargets.removeLast();
    statementBlock.addSuccessor(data.currentBlock);
    data.currentBlock = loopback;
    build(node.getVariable(), data);
    data.currentBlock = data.createBlock(data.currentBlock);
    build(node.getEnumerable(), data);
    data.currentBlock = data.createBlock(data.currentBlock);
  }

  private static void buildWithStatement(WithStatementNode node, ControlFlowGraphData data) {
    build(node.getStatement(), data);
    data.currentBlock = data.createBlock(data.currentBlock);
    build(node.getTargets(), data);
    data.currentBlock = data.createBlock(data.currentBlock);
  }

  private static void addContinueTarget(ControlFlowGraphData data, Block target) {
    data.continueTargets.addLast(target);
  }

  private static void buildBinaryExpression(BinaryExpressionNode node, ControlFlowGraphData data) {
    boolean booleanType = node.getType().isBoolean();
    if (booleanType && node.getOperator() == BinaryOperator.AND) {
      buildBooleanAnd(data, node);
    } else if (booleanType && node.getOperator() == BinaryOperator.OR) {
      buildBooleanOr(data, node);
    } else {
      data.currentBlock.addElement(node);
      build(node.getRight(), data);
      build(node.getLeft(), data);
    }
  }

  private static void buildUnaryExpression(UnaryExpressionNode node, ControlFlowGraphData data) {
    data.currentBlock.addElement(node);
    build(node.getOperand(), data);
  }

  private static void buildCondition(
      ControlFlowGraphData data, ExpressionNode node, Block trueBlock, Block falseBlock) {
    node = node.skipParentheses();
    if (node instanceof BinaryExpressionNode) {
      if (((BinaryExpressionNode) node).getOperator() == BinaryOperator.OR) {
        buildConditionOr(data, (BinaryExpressionNode) node, trueBlock, falseBlock);
      } else if (((BinaryExpressionNode) node).getOperator() == BinaryOperator.AND) {
        buildConditionAnd(data, (BinaryExpressionNode) node, trueBlock, falseBlock);
      } else {
        build(node, data);
      }
    } else if (node instanceof ParenthesizedExpressionNode) {
      buildCondition(
          data, ((ParenthesizedExpressionNode) node).getExpression(), trueBlock, falseBlock);
    } else {
      build(node, data);
    }
  }

  private static void buildConditionOr(
      ControlFlowGraphData data, BinaryExpressionNode node, Block trueBlock, Block falseBlock) {
    // RHS
    buildCondition(data, node.getRight(), trueBlock, falseBlock);
    Block newFalseBlock = data.currentBlock;
    // LHS
    data.currentBlock = data.createBranch(node, trueBlock, newFalseBlock);
    buildCondition(data, node.getLeft(), trueBlock, newFalseBlock);
  }

  private static void buildBooleanOr(ControlFlowGraphData data, BinaryExpressionNode node) {
    Block trueBlock = data.currentBlock;
    data.currentBlock = data.createBlock(trueBlock);
    build(node.getRight(), data);
    buildConditionalBinaryLHS(data, node, trueBlock, data.currentBlock);
  }

  private static void buildConditionAnd(
      ControlFlowGraphData data, BinaryExpressionNode node, Block trueBlock, Block falseBlock) {
    // RHS
    buildCondition(data, node.getRight(), trueBlock, falseBlock);
    Block newTrueBlock = data.currentBlock;
    // LHS
    data.currentBlock = data.createBranch(node, newTrueBlock, falseBlock);
    buildCondition(data, node.getLeft(), newTrueBlock, falseBlock);
  }

  private static void buildBooleanAnd(ControlFlowGraphData data, BinaryExpressionNode node) {
    Block falseBlock = data.currentBlock;
    data.currentBlock = data.createBlock(falseBlock);
    build(node.getRight(), data);
    buildConditionalBinaryLHS(data, node, data.currentBlock, falseBlock);
  }

  private static void buildConditionalBinaryLHS(
      ControlFlowGraphData data, BinaryExpressionNode tree, Block trueBlock, Block falseBlock) {
    data.currentBlock = data.createBlock();
    Block toComplete = data.currentBlock;
    build(tree.getLeft(), data);
    toComplete.setTerminator(TerminatorKind.NODE, tree);
    toComplete.addFalseSuccessor(falseBlock);
    toComplete.addTrueSuccessor(trueBlock);
  }

  private static Block createUnconditionalJump(
      ControlFlowGraphData data,
      TerminatorKind terminatorKind,
      DelphiNode terminator,
      Block target,
      Block successorWithoutJump) {
    Block result = data.createBlock();
    result.setTerminator(terminatorKind, terminator);
    if (target != null) {
      if (target == data.exitBlock()) {
        result.addExitSuccessor(target);
      } else {
        result.addSuccessor(target);
      }
    }
    result.setSuccessorWithoutJump(successorWithoutJump);
    return result;
  }
}
