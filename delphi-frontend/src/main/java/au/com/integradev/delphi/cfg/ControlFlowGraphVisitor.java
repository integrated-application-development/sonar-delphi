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

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import au.com.integradev.delphi.cfg.api.Block;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.plugins.communitydelphi.api.ast.AnonymousMethodNode;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentNode;
import org.sonar.plugins.communitydelphi.api.ast.ArrayConstructorNode;
import org.sonar.plugins.communitydelphi.api.ast.AsmStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.CaseItemStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.CaseStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.CommonDelphiNode;
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
import org.sonar.plugins.communitydelphi.api.ast.ForStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ForToStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.GotoStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.IfStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.IntegerLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.LabelStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationListNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.NilLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.RaiseStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.RangeExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.RealLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.RepeatStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.SimpleNameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementListNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
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

/**
 * This visitor constructs a control flow graph. Generally, the statements and elements are
 * traversed backward simplify the construction of a directed graph. `Block`s typically are ordered
 * in way they are evaluated.
 */
class ControlFlowGraphVisitor implements DelphiParserVisitor<ControlFlowGraphImpl> {

  // Literals / Block elements
  // These nodes get added to the current block

  @Override
  public ControlFlowGraphImpl visit(IntegerLiteralNode node, ControlFlowGraphImpl data) {
    data.addElement(node);
    return data;
  }

  @Override
  public ControlFlowGraphImpl visit(RealLiteralNode node, ControlFlowGraphImpl data) {
    data.addElement(node);
    return data;
  }

  @Override
  public ControlFlowGraphImpl visit(NilLiteralNode node, ControlFlowGraphImpl data) {
    data.addElement(node);
    return data;
  }

  @Override
  public ControlFlowGraphImpl visit(SimpleNameDeclarationNode node, ControlFlowGraphImpl data) {
    data.addElement(node);
    return data;
  }

  @Override
  public ControlFlowGraphImpl visit(RangeExpressionNode node, ControlFlowGraphImpl data) {
    build(node.getLowExpression(), data);
    return build(node.getHighExpression(), data);
  }

  @Override
  public ControlFlowGraphImpl visit(ArrayConstructorNode node, ControlFlowGraphImpl data) {
    return build(node.getElements(), data);
  }

  @Override
  public ControlFlowGraphImpl visit(ForLoopVarDeclarationNode node, ControlFlowGraphImpl data) {
    return build(node.getNameDeclarationNode(), data);
  }

  @Override
  public ControlFlowGraphImpl visit(ForLoopVarReferenceNode node, ControlFlowGraphImpl data) {
    return build(node.getNameReference(), data);
  }

  @Override
  public ControlFlowGraphImpl visit(ArgumentNode node, ControlFlowGraphImpl data) {
    return build(node.getExpression(), data);
  }

  /**
   * <code>NameReferenceNode</code> has overloaded meanings in Delphi. The control flow intrinsics
   * are handled individually.
   */
  @Override
  public ControlFlowGraphImpl visit(NameReferenceNode node, ControlFlowGraphImpl data) {
    if (!(node.getLastName().getNameDeclaration() instanceof RoutineNameDeclaration)) {
      data.addElement(node);
      return data;
    }
    String routineName =
        ((RoutineNameDeclaration) node.getLastName().getNameDeclaration()).fullyQualifiedName();
    switch (routineName) {
      case "System.Exit":
        data.nextBlock(
            data.buildNewBlock().withJump(node, data.getExitBlock(), data.getCurrentBlock()));
        return data;
      case "System.Break":
        return buildControlFlowStatement(node, data.getBreakTarget(), data);
      case "System.Halt":
        data.nextBlock(data.buildNewBlock().withTerminator(node));
        return data;
      case "System.Continue":
        return buildControlFlowStatement(node, data.getContinueTarget(), data);
      default:
        handleExceptionalPaths(data);
        data.addElement(node);
        break;
    }

    return data;
  }

  private ControlFlowGraphImpl buildControlFlowStatement(
      DelphiNode node, Block target, ControlFlowGraphImpl data) {
    if (target == null) {
      throw new IllegalStateException(
          String.format("'%s' statement not in loop statement", node.getImage()));
    }
    data.nextBlock(data.buildNewBlock().withJump(node, target, data.getCurrentBlock()));
    return data;
  }

  private void handleExceptionalPaths(ControlFlowGraphImpl data) {
    if (!data.inTryContext()) {
      // Only consider routines as potentially exceptional if in a `try` context.
      return;
    }

    Set<Block> exceptions = data.getAllCatchTargets();
    data.nextBlock(data.buildNewBlock().withExceptions(data.getCurrentBlock(), exceptions));
  }

  /** Overridden to ensure <code>NodeDeclaration</code>s are added in the correct order */
  @Override
  public ControlFlowGraphImpl visit(NameDeclarationListNode node, ControlFlowGraphImpl data) {
    return build(node.getDeclarations(), data);
  }

  /** <code>ExpressionNode</code> covers the <code>inherited</code> expressions */
  @Override
  public ControlFlowGraphImpl visit(ExpressionNode node, ControlFlowGraphImpl data) {
    if (!ExpressionNodeUtils.isInherited(node)) {
      return DelphiParserVisitor.super.visit(node, data);
    }

    // Arguments
    ArgumentListNode arguments = node.getFirstChildOfType(ArgumentListNode.class);
    if (arguments != null) {
      build(arguments.getArgumentNodes(), data);
    }

    // Possible name
    build(node.getFirstChildOfType(NameReferenceNode.class), data);

    // `inherited`
    data.addElement(node.getFirstChildOfType(CommonDelphiNode.class));
    return data;
  }

  // Statements

  @Override
  public ControlFlowGraphImpl visit(StatementListNode node, ControlFlowGraphImpl data) {
    return build(node.getStatements(), data);
  }

  /**
   * From the condition there are two successors, a true branch and a false branch. Both branches
   * will have successors of the block that comes after.
   *
   * <pre>{@code
   * if Condition then ThenBlock else ElseBlock;
   * }</pre>
   *
   * maps to
   *
   * <pre>
   *              ┌─> true ──> `ThenBlock` ─┐
   * `Condition` ─┤                         ├─> after
   *              └─> false ─> `ElseBlock` ─┘
   * </pre>
   */
  @Override
  public ControlFlowGraphImpl visit(IfStatementNode node, ControlFlowGraphImpl data) {
    Block after = data.getCurrentBlock();

    // process `else`
    Block elseBlock = after;
    if (node.getElseStatement() != null) {
      StatementNode elseStatement = node.getElseStatement();
      if (!(elseStatement instanceof IfStatementNode)) {
        data.nextBlockTo(after);
      }
      elseStatement.accept(this, data);
      elseBlock = data.getCurrentBlock();
    }

    // process `then`
    data.nextBlockTo(after);
    build(node.getThenStatement(), data);
    Block thenBlock = data.getCurrentBlock();

    // process condition
    data.nextBlock(data.buildNewBlock().withBranch(node, thenBlock, elseBlock));
    return buildCondition(data, node.getGuardExpression(), thenBlock, elseBlock);
  }

  private ControlFlowGraphImpl buildCondition(
      ControlFlowGraphImpl data, ExpressionNode node, Block trueBlock, Block falseBlock) {
    node = node.skipParentheses();
    if (!(node instanceof BinaryExpressionNode)) {
      return build(node, data);
    }
    BinaryExpressionNode binaryExpression = (BinaryExpressionNode) node;
    BinaryOperator operator = binaryExpression.getOperator();
    if (operator == BinaryOperator.OR) {
      return buildConditionOr(data, binaryExpression, trueBlock, falseBlock);
    } else if (operator == BinaryOperator.AND) {
      return buildConditionAnd(data, binaryExpression, trueBlock, falseBlock);
    }
    return build(node, data);
  }

  private ControlFlowGraphImpl buildConditionAnd(
      ControlFlowGraphImpl data, BinaryExpressionNode node, Block trueBlock, Block falseBlock) {
    // RHS
    buildCondition(data, node.getRight(), trueBlock, falseBlock);
    Block newTrueBlock = data.getCurrentBlock();
    // LHS
    data.nextBlock(data.buildNewBlock().withBranch(node, newTrueBlock, falseBlock));
    return buildCondition(data, node.getLeft(), newTrueBlock, falseBlock);
  }

  private ControlFlowGraphImpl buildConditionOr(
      ControlFlowGraphImpl data, BinaryExpressionNode node, Block trueBlock, Block falseBlock) {
    // RHS
    buildCondition(data, node.getRight(), trueBlock, falseBlock);
    Block newFalseBlock = data.getCurrentBlock();
    // LHS
    data.nextBlock(data.buildNewBlock().withBranch(node, trueBlock, newFalseBlock));
    return buildCondition(data, node.getLeft(), trueBlock, newFalseBlock);
  }

  @Override
  public ControlFlowGraphImpl visit(VarStatementNode node, ControlFlowGraphImpl data) {
    build(node.getNameDeclarationList(), data);
    return build(node.getExpression(), data);
  }

  @Override
  public ControlFlowGraphImpl visit(ConstStatementNode node, ControlFlowGraphImpl data) {
    build(node.getNameDeclarationNode(), data);
    return build(node.getExpression(), data);
  }

  /**
   * The case's selector expression has a successors of each case, and the optional else block.
   *
   * <pre>{@code
   * case Selector of
   *   A: Body1;
   *   B: Body2;
   * else
   *   C: Body3;
   * end;
   * }</pre>
   *
   * maps to
   *
   * <pre>
   *             ┌─>   `A`  => `Body1` ─┐
   * `Selector` ─┼─>   `B`  => `Body2` ─┼─> after
   *             └─> `else` => `Body3` ─┘
   * </pre>
   */
  @Override
  public ControlFlowGraphImpl visit(CaseStatementNode node, ControlFlowGraphImpl data) {
    Block after = data.getCurrentBlock();

    // Selector
    Block caseBlock = data.nextBlockToCurrent();

    List<ExpressionNode> caseLabels =
        node.getCaseItems().stream()
            .flatMap(caseNode -> caseNode.getExpressions().stream())
            .collect(Collectors.toList());
    build(caseLabels, data);
    build(node.getSelectorExpression(), data);
    Block conditionBlock = data.getCurrentBlock();

    Set<Block> caseSuccessors = new HashSet<>();

    // Else
    if (node.getElseBlockNode() != null) {
      data.nextBlockTo(after);
      build(node.getElseBlockNode().getStatementList(), data);
      caseSuccessors.add(data.getCurrentBlock());
    } else {
      // If there is no `else` block the statement can be jumped
      caseSuccessors.add(after);
    }

    // Cases
    for (StatementNode statement :
        ListUtils.reverse(
            node.getCaseItems().stream()
                .map(CaseItemStatementNode::getStatement)
                .collect(Collectors.toList()))) {
      data.nextBlockTo(after);
      build(statement, data);
      caseSuccessors.add(data.getCurrentBlock());
    }

    data.updateBlock(data.buildReplacement(caseBlock).withCases(node, caseSuccessors));
    data.setCurrentBlock(conditionBlock);
    return data;
  }

  /**
   * Repeat statements flow through the body first, then consult the condition as to where to go
   * next.
   *
   * <pre>{@code
   * repeat
   *   Body;
   * until Condition;
   * }</pre>
   *
   * maps to
   *
   * <pre>
   *                        ┌─> true ────┐
   * `Body` ─> `Condition` ─┴─> false ─┐ └─> after
   *   ^───────────────────────────────┘
   * </pre>
   */
  @Override
  public ControlFlowGraphImpl visit(RepeatStatementNode node, ControlFlowGraphImpl data) {
    Block after = data.getCurrentBlock();
    // Create a placeholder for the conditional block
    Block loopback = data.nextBlockTo(after);

    // Condition
    data.nextBlock(data.buildNewBlock().withBranch(node, after, loopback));
    buildCondition(data, node.getGuardExpression(), after, loopback);

    // Body
    data.pushLoopContext(data.getCurrentBlock(), after);
    data.nextBlockToCurrent();
    build(node.getStatementList(), data);
    data.popLoopContext();

    data.updateBlock(data.buildReplacement(loopback).withSuccessor(data.getCurrentBlock()));
    data.nextBlockToCurrent();
    return data;
  }

  /**
   * While loops flow through the condition first, then based on its value either enter the loop or
   * continue on.
   *
   * <pre>{@code
   * while Condition do
   *   Body;
   * }</pre>
   *
   * maps to
   *
   * <pre>
   *              ┌─> false ─────────────┐
   * `Condition` ─┴─> true ──> `Body` ─┐ └─> after
   *     ^─────────────────────────────┘
   * </pre>
   */
  @Override
  public ControlFlowGraphImpl visit(WhileStatementNode node, ControlFlowGraphImpl data) {
    Block after = data.getCurrentBlock();
    // Create a placeholder for the conditional block
    Block loopback = data.nextBlockTo(after);

    // Body
    data.nextBlockTo(loopback);
    data.pushLoopContext(loopback, after);
    build(node.getStatement(), data);
    data.popLoopContext();
    Block body = data.getCurrentBlock();

    // Condition
    data.nextBlock(data.buildNewBlock().withBranch(node, body, after));
    buildCondition(data, node.getGuardExpression(), body, after);

    data.updateBlock(data.buildReplacement(loopback).withSuccessor(data.getCurrentBlock()));
    data.nextBlockToCurrent();
    return data;
  }

  /**
   * For to/downto loops evaluate in order the low value expression, the high expression, the body,
   * and the variable. The variable has branching behaviour based on whether there is a next element
   * in the range.
   *
   * <pre>{@code
   * for A := B to C do
   *   Body;
   * }</pre>
   *
   * maps to
   *
   * <pre>
   * `B` ─> `C` ─> `Body` ─> `A` ─┬─> false ─> after
   *                 ^─── true <──┘
   * </pre>
   */
  @Override
  public ControlFlowGraphImpl visit(ForToStatementNode node, ControlFlowGraphImpl data) {
    return buildForLoop(
        node,
        List.of(node.getVariable(), node.getTargetExpression(), node.getInitializerExpression()),
        data);
  }

  /**
   * For in loops evaluate in order the enumerable, the body, and the variable. The variable has
   * branching behaviour based on whether there is a next element in the enumerable.
   *
   * <pre>{@code
   * for A in B do
   *   Body;
   * }</pre>
   *
   * maps to
   *
   * <pre>
   * `B` ─> `Body` ─> `A` ─┬─> false ─> after
   *          ^─── true <──┘
   * </pre>
   */
  @Override
  public ControlFlowGraphImpl visit(ForInStatementNode node, ControlFlowGraphImpl data) {
    return buildForLoop(node, List.of(node.getVariable(), node.getEnumerable()), data);
  }

  private ControlFlowGraphImpl buildForLoop(
      ForStatementNode node, List<DelphiNode> parts, ControlFlowGraphImpl data) {
    Block after = data.getCurrentBlock();
    // Create a placeholder for the conditional block
    Block loopback = data.nextBlockToCurrent();
    data.nextBlockToCurrent();

    data.pushLoopContext(loopback, after);
    build(node.getStatement(), data);
    data.popLoopContext();

    data.updateBlock(
        data.buildReplacement(loopback).withBranch(node, data.getCurrentBlock(), after));

    data.setCurrentBlock(loopback);
    parts.forEach(
        part -> {
          build(part, data);
          data.nextBlockToCurrent();
        });
    return data;
  }

  @Override
  public ControlFlowGraphImpl visit(WithStatementNode node, ControlFlowGraphImpl data) {
    build(node.getStatement(), data);
    data.nextBlockToCurrent();
    build(node.getTargets(), data);
    data.nextBlockToCurrent();
    return data;
  }

  /**
   * Try statements add handling for exceptions. There is a direct path from the `finally` block to
   * the local exit block. This path would be used when another control flow altering statement is
   * used, such as `Exit`, `Break`, and exceptions. Within try statements, routine invocations gain
   * a successors to the catches/finally block.
   */
  @Override
  public ControlFlowGraphImpl visit(TryStatementNode node, ControlFlowGraphImpl data) {
    data.nextBlockToCurrent();
    // Finally
    FinallyBlockNode finallyNode = node.getFinallyBlock();
    if (finallyNode != null) {
      data.nextBlock(
          data.buildNewBlock().withExitPath(data.getCurrentBlock(), data.getExitBlock()));
      build(finallyNode.getStatementList(), data);
      data.pushLoopContext(data.getCurrentBlock(), data.getCurrentBlock());
      data.pushExitBlock(data.getCurrentBlock());
    }
    Block finallyOrEndBlock = data.getCurrentBlock();
    Block beforeFinally = data.nextBlockToCurrent();

    // Exception catches
    List<Entry<Type, Block>> catches = new ArrayList<>();
    Block elseBlock = null;

    ExceptBlockNode exceptBlock = node.getExceptBlock();
    if (exceptBlock != null) {
      if (exceptBlock.isBareExcept()) {
        data.nextBlockTo(finallyOrEndBlock);
        build(exceptBlock.getStatementList(), data);
        elseBlock = data.getCurrentBlock();
      } else if (exceptBlock.getElseBlock() != null) {
        data.nextBlockTo(finallyOrEndBlock);
        build(exceptBlock.getElseBlock().getStatementList(), data);
        elseBlock = data.getCurrentBlock();
      }
      if (exceptBlock.hasHandlers()) {
        for (ExceptItemNode exceptItem : ListUtils.reverse(exceptBlock.getHandlers())) {
          data.nextBlockTo(finallyOrEndBlock);
          build(exceptItem.getStatement(), data);
          build(exceptItem.getExceptionName(), data);
          catches.add(
              0,
              new SimpleEntry<>(exceptItem.getExceptionType().getType(), data.getCurrentBlock()));
        }
      }
    }

    // Body
    data.setCurrentBlock(beforeFinally);

    data.pushTryContext(catches, elseBlock);
    build(node.getStatementList(), data);
    data.popTryContext();

    data.nextBlockToCurrent();
    data.addElement(node);

    if (finallyNode != null) {
      data.popExitBlock();
    }
    return data;
  }

  /**
   * `raise` statements jump directly to the handling exception or exit block. Bare raise statements
   * have successors of all exceptional targets.
   */
  @Override
  public ControlFlowGraphImpl visit(RaiseStatementNode node, ControlFlowGraphImpl data) {
    if (node.getRaiseExpression() == null) {
      Set<Block> exceptions = data.getAllCatchTargets();
      data.nextBlock(data.buildNewBlock().withExceptions(data.getCurrentBlock(), exceptions));
      data.addElement(node);
      return data;
    }

    Type raiseType = node.getRaiseExpression().getType();
    Block jumpTarget = data.getCatchTarget(raiseType);
    data.nextBlock(data.buildNewBlock().withJump(node, jumpTarget, data.getCurrentBlock()));
    return build(node.getRaiseExpression(), data);
  }

  /** Label statements create a new block as they allow for the control flow to jump to them. */
  @Override
  public ControlFlowGraphImpl visit(LabelStatementNode node, ControlFlowGraphImpl data) {
    build(node.getStatement(), data);
    data.addLabel(node);
    data.nextBlockToCurrent();
    return data;
  }

  /** `goto` statements have a successor of the label they jump to. */
  @Override
  public ControlFlowGraphImpl visit(GotoStatementNode node, ControlFlowGraphImpl data) {
    data.addGoto(node);
    return data;
  }

  @Override
  public ControlFlowGraphImpl visit(AssignmentStatementNode node, ControlFlowGraphImpl data) {
    build(node.getAssignee(), data);
    return build(node.getValue(), data);
  }

  @Override
  public ControlFlowGraphImpl visit(ExpressionStatementNode node, ControlFlowGraphImpl data) {
    return build(node.getExpression(), data);
  }

  // Expressions

  @Override
  public ControlFlowGraphImpl visit(UnaryExpressionNode node, ControlFlowGraphImpl data) {
    data.addElement(node);
    return build(node.getOperand(), data);
  }

  @Override
  public ControlFlowGraphImpl visit(BinaryExpressionNode node, ControlFlowGraphImpl data) {
    boolean isBooleanExpr = node.getType().isBoolean();
    if (isBooleanExpr && node.getOperator() == BinaryOperator.AND) {
      return buildBooleanAnd(node, data);
    } else if (isBooleanExpr && node.getOperator() == BinaryOperator.OR) {
      return buildBooleanOr(node, data);
    }

    data.addElement(node);
    build(node.getRight(), data);
    build(node.getLeft(), data);
    return data;
  }

  /**
   * Boolean `and` expressions represent the short-circuiting behaviour. `A and B` is as follows:
   *
   * <pre>
   *      ┌─> true ─> `B` ─┐
   * `A` ─┤                ├─> after
   *      └─> false ───────┘
   * </pre>
   */
  private ControlFlowGraphImpl buildBooleanAnd(
      BinaryExpressionNode node, ControlFlowGraphImpl data) {
    Block falseBlock = data.getCurrentBlock();
    data.nextBlockTo(falseBlock);
    build(node.getRight(), data);
    return buildBooleanLHS(data, node, data.getCurrentBlock(), falseBlock);
  }

  /**
   * Boolean `or` expressions represent the short-circuiting behaviour. `A or B` is as follows:
   *
   * <pre>
   *      ┌─> true ──> ─────┐
   * `A` ─┤                 ├─> after
   *      └─> false ─> `B` ─┘
   * </pre>
   */
  private ControlFlowGraphImpl buildBooleanOr(
      BinaryExpressionNode node, ControlFlowGraphImpl data) {
    Block trueBlock = data.getCurrentBlock();
    data.nextBlockTo(trueBlock);
    build(node.getRight(), data);
    return buildBooleanLHS(data, node, trueBlock, data.getCurrentBlock());
  }

  private ControlFlowGraphImpl buildBooleanLHS(
      ControlFlowGraphImpl data, BinaryExpressionNode node, Block trueBlock, Block falseBlock) {
    data.nextBlock(data.buildNewBlock().withBranch(node, trueBlock, falseBlock));
    return build(node.getLeft(), data);
  }

  // Exclusions

  /**
   * Anonymous methods have their own associated control flow graph. One that is separate to the
   * current one being constructed.
   */
  @Override
  public ControlFlowGraphImpl visit(AnonymousMethodNode node, ControlFlowGraphImpl data) {
    return data;
  }

  /** Assembly control flow graphs are not supported. */
  @Override
  public ControlFlowGraphImpl visit(AsmStatementNode node, ControlFlowGraphImpl data) {
    return data;
  }

  // Helpers

  private ControlFlowGraphImpl build(DelphiNode node, ControlFlowGraphImpl data) {
    if (node == null) {
      return data;
    }
    return node.accept(this, data);
  }

  private <T extends DelphiNode> ControlFlowGraphImpl build(
      List<T> nodes, ControlFlowGraphImpl data) {
    ListUtils.reverse(nodes).forEach(node -> build(node, data));
    return data;
  }
}
