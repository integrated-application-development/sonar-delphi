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

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import au.com.integradev.delphi.cfg.block.BuilderBlock;
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
 * This visitor populates the <code>ControlFlowGraphBuilder</code> to construct a control flow
 * graph. Generally, the statements and elements are traversed backward simplify the construction of
 * a directed graph. `Block`s typically are ordered in way they are evaluated.
 */
class ControlFlowGraphVisitor implements DelphiParserVisitor<ControlFlowGraphBuilder> {

  // Literals / Block elements
  // These nodes get added to the current block

  @Override
  public ControlFlowGraphBuilder visit(IntegerLiteralNode node, ControlFlowGraphBuilder builder) {
    builder.addElement(node);
    return builder;
  }

  @Override
  public ControlFlowGraphBuilder visit(RealLiteralNode node, ControlFlowGraphBuilder builder) {
    builder.addElement(node);
    return builder;
  }

  @Override
  public ControlFlowGraphBuilder visit(NilLiteralNode node, ControlFlowGraphBuilder builder) {
    builder.addElement(node);
    return builder;
  }

  @Override
  public ControlFlowGraphBuilder visit(
      SimpleNameDeclarationNode node, ControlFlowGraphBuilder builder) {
    builder.addElement(node);
    return builder;
  }

  @Override
  public ControlFlowGraphBuilder visit(RangeExpressionNode node, ControlFlowGraphBuilder builder) {
    build(node.getLowExpression(), builder);
    return build(node.getHighExpression(), builder);
  }

  @Override
  public ControlFlowGraphBuilder visit(ArrayConstructorNode node, ControlFlowGraphBuilder builder) {
    return build(node.getElements(), builder);
  }

  @Override
  public ControlFlowGraphBuilder visit(
      ForLoopVarDeclarationNode node, ControlFlowGraphBuilder builder) {
    return build(node.getNameDeclarationNode(), builder);
  }

  @Override
  public ControlFlowGraphBuilder visit(
      ForLoopVarReferenceNode node, ControlFlowGraphBuilder builder) {
    return build(node.getNameReference(), builder);
  }

  @Override
  public ControlFlowGraphBuilder visit(ArgumentNode node, ControlFlowGraphBuilder builder) {
    return build(node.getExpression(), builder);
  }

  /**
   * <code>NameReferenceNode</code> has overloaded meanings in Delphi. The control flow intrinsics
   * are handled individually.
   */
  @Override
  public ControlFlowGraphBuilder visit(NameReferenceNode node, ControlFlowGraphBuilder builder) {
    if (!(node.getLastName().getNameDeclaration() instanceof RoutineNameDeclaration)) {
      builder.addElement(node);
      return builder;
    }
    String routineName =
        ((RoutineNameDeclaration) node.getLastName().getNameDeclaration()).fullyQualifiedName();
    switch (routineName) {
      case "System.Exit":
        builder.nextBlock(
            newBlock().withJump(node, builder.getExitBlock(), builder.getCurrentBlock()));
        return builder;
      case "System.Break":
        return buildControlFlowStatement(node, builder.getBreakTarget(), builder);
      case "System.Halt":
        builder.nextBlock(newBlock().withTerminator(node));
        return builder;
      case "System.Continue":
        return buildControlFlowStatement(node, builder.getContinueTarget(), builder);
      default:
        handleExceptionalPaths(builder);
        builder.addElement(node);
        break;
    }

    return builder;
  }

  private ControlFlowGraphBuilder buildControlFlowStatement(
      DelphiNode node, BuilderBlock target, ControlFlowGraphBuilder builder) {
    if (target == null) {
      throw new IllegalStateException(
          String.format("'%s' statement not in loop statement", node.getImage()));
    }
    builder.nextBlock(newBlock().withJump(node, target, builder.getCurrentBlock()));
    return builder;
  }

  private void handleExceptionalPaths(ControlFlowGraphBuilder builder) {
    if (!builder.inTryContext()) {
      // Only consider routines as potentially exceptional if in a `try` context.
      return;
    }

    Set<BuilderBlock> exceptions = builder.getAllCatchTargets();
    builder.nextBlock(newBlock().withExceptions(builder.getCurrentBlock(), exceptions));
  }

  /** Overridden to ensure <code>NodeDeclaration</code>s are added in the correct order */
  @Override
  public ControlFlowGraphBuilder visit(
      NameDeclarationListNode node, ControlFlowGraphBuilder builder) {
    return build(node.getDeclarations(), builder);
  }

  /** <code>ExpressionNode</code> covers the <code>inherited</code> expressions */
  @Override
  public ControlFlowGraphBuilder visit(ExpressionNode node, ControlFlowGraphBuilder builder) {
    if (!ExpressionNodeUtils.isInherited(node)) {
      return DelphiParserVisitor.super.visit(node, builder);
    }

    // Arguments
    ArgumentListNode arguments = node.getFirstChildOfType(ArgumentListNode.class);
    if (arguments != null) {
      build(arguments.getArgumentNodes(), builder);
    }

    // Possible name
    build(node.getFirstChildOfType(NameReferenceNode.class), builder);

    // `inherited`
    builder.addElement(node.getFirstChildOfType(CommonDelphiNode.class));
    return builder;
  }

  // Statements

  @Override
  public ControlFlowGraphBuilder visit(StatementListNode node, ControlFlowGraphBuilder builder) {
    return build(node.getStatements(), builder);
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
  public ControlFlowGraphBuilder visit(IfStatementNode node, ControlFlowGraphBuilder builder) {
    BuilderBlock after = builder.getCurrentBlock();

    // process `else`
    BuilderBlock elseBlock = after;
    if (node.getElseStatement() != null) {
      StatementNode elseStatement = node.getElseStatement();
      if (!(elseStatement instanceof IfStatementNode)) {
        builder.nextBlockTo(after);
      }
      elseStatement.accept(this, builder);
      elseBlock = builder.getCurrentBlock();
    }

    // process `then`
    builder.nextBlockTo(after);
    build(node.getThenStatement(), builder);
    BuilderBlock thenBlock = builder.getCurrentBlock();

    // process condition
    builder.nextBlock(newBlock().withBranch(node, thenBlock, elseBlock));
    return buildCondition(builder, node.getGuardExpression(), thenBlock, elseBlock);
  }

  private ControlFlowGraphBuilder buildCondition(
      ControlFlowGraphBuilder builder,
      ExpressionNode node,
      BuilderBlock trueBlock,
      BuilderBlock falseBlock) {
    node = node.skipParentheses();
    if (!(node instanceof BinaryExpressionNode)) {
      return build(node, builder);
    }
    BinaryExpressionNode binaryExpression = (BinaryExpressionNode) node;
    BinaryOperator operator = binaryExpression.getOperator();
    if (operator == BinaryOperator.OR) {
      return buildConditionOr(builder, binaryExpression, trueBlock, falseBlock);
    } else if (operator == BinaryOperator.AND) {
      return buildConditionAnd(builder, binaryExpression, trueBlock, falseBlock);
    }
    return build(node, builder);
  }

  private ControlFlowGraphBuilder buildConditionAnd(
      ControlFlowGraphBuilder builder,
      BinaryExpressionNode node,
      BuilderBlock trueBlock,
      BuilderBlock falseBlock) {
    // RHS
    buildCondition(builder, node.getRight(), trueBlock, falseBlock);
    BuilderBlock newTrueBlock = builder.getCurrentBlock();
    // LHS
    builder.nextBlock(newBlock().withBranch(node, newTrueBlock, falseBlock));
    return buildCondition(builder, node.getLeft(), newTrueBlock, falseBlock);
  }

  private ControlFlowGraphBuilder buildConditionOr(
      ControlFlowGraphBuilder builder,
      BinaryExpressionNode node,
      BuilderBlock trueBlock,
      BuilderBlock falseBlock) {
    // RHS
    buildCondition(builder, node.getRight(), trueBlock, falseBlock);
    BuilderBlock newFalseBlock = builder.getCurrentBlock();
    // LHS
    builder.nextBlock(newBlock().withBranch(node, trueBlock, newFalseBlock));
    return buildCondition(builder, node.getLeft(), trueBlock, newFalseBlock);
  }

  @Override
  public ControlFlowGraphBuilder visit(VarStatementNode node, ControlFlowGraphBuilder builder) {
    build(node.getNameDeclarationList(), builder);
    return build(node.getExpression(), builder);
  }

  @Override
  public ControlFlowGraphBuilder visit(ConstStatementNode node, ControlFlowGraphBuilder builder) {
    build(node.getNameDeclarationNode(), builder);
    return build(node.getExpression(), builder);
  }

  /**
   * The case's selector expression has a successors of each case, and the optional else block.
   *
   * <pre>{@code
   * case Selector of
   *   A: Body1;
   *   B: Body2;
   * else
   *   Body3;
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
  public ControlFlowGraphBuilder visit(CaseStatementNode node, ControlFlowGraphBuilder builder) {
    BuilderBlock after = builder.getCurrentBlock();

    // Selector
    BuilderBlock caseBlock = builder.nextBlockToCurrent();

    List<ExpressionNode> caseLabels =
        node.getCaseItems().stream()
            .flatMap(caseNode -> caseNode.getExpressions().stream())
            .collect(Collectors.toList());
    build(caseLabels, builder);
    build(node.getSelectorExpression(), builder);
    BuilderBlock conditionBlock = builder.getCurrentBlock();

    Set<BuilderBlock> caseSuccessors = new HashSet<>();

    // Else
    if (node.getElseBlockNode() != null) {
      builder.nextBlockTo(after);
      build(node.getElseBlockNode().getStatementList(), builder);
      caseSuccessors.add(builder.getCurrentBlock());
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
      builder.nextBlockTo(after);
      build(statement, builder);
      caseSuccessors.add(builder.getCurrentBlock());
    }

    caseBlock.update(newBlock().withCases(node, caseSuccessors));
    builder.setCurrentBlock(conditionBlock);
    return builder;
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
  public ControlFlowGraphBuilder visit(RepeatStatementNode node, ControlFlowGraphBuilder builder) {
    BuilderBlock after = builder.getCurrentBlock();
    // Create a placeholder for the conditional block
    BuilderBlock loopback = builder.nextBlockTo(after);

    // Condition
    builder.nextBlock(newBlock().withBranch(node, after, loopback));
    buildCondition(builder, node.getGuardExpression(), after, loopback);

    // Body
    builder.pushLoopContext(builder.getCurrentBlock(), after);
    builder.nextBlockToCurrent();
    build(node.getStatementList(), builder);
    builder.popLoopContext();

    loopback.update(newBlock().withSuccessor(builder.getCurrentBlock()));
    builder.nextBlockToCurrent();
    return builder;
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
  public ControlFlowGraphBuilder visit(WhileStatementNode node, ControlFlowGraphBuilder builder) {
    BuilderBlock after = builder.getCurrentBlock();
    // Create a placeholder for the conditional block
    BuilderBlock loopback = builder.nextBlockTo(after);

    // Body
    builder.nextBlockTo(loopback);
    builder.pushLoopContext(loopback, after);
    build(node.getStatement(), builder);
    builder.popLoopContext();
    BuilderBlock body = builder.getCurrentBlock();

    // Condition
    builder.nextBlock(newBlock().withBranch(node, body, after));
    buildCondition(builder, node.getGuardExpression(), body, after);

    loopback.update(newBlock().withSuccessor(builder.getCurrentBlock()));
    builder.nextBlockToCurrent();
    return builder;
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
  public ControlFlowGraphBuilder visit(ForToStatementNode node, ControlFlowGraphBuilder builder) {
    return buildForLoop(
        node,
        List.of(node.getVariable(), node.getTargetExpression(), node.getInitializerExpression()),
        builder);
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
  public ControlFlowGraphBuilder visit(ForInStatementNode node, ControlFlowGraphBuilder builder) {
    return buildForLoop(node, List.of(node.getVariable(), node.getEnumerable()), builder);
  }

  private ControlFlowGraphBuilder buildForLoop(
      ForStatementNode node, List<DelphiNode> parts, ControlFlowGraphBuilder builder) {
    BuilderBlock after = builder.getCurrentBlock();
    // Create a placeholder for the conditional block
    BuilderBlock loopback = builder.nextBlockToCurrent();
    builder.nextBlockToCurrent();

    builder.pushLoopContext(loopback, after);
    build(node.getStatement(), builder);
    builder.popLoopContext();

    loopback.update(newBlock().withBranch(node, builder.getCurrentBlock(), after));

    builder.setCurrentBlock(loopback);
    parts.forEach(
        part -> {
          build(part, builder);
          builder.nextBlockToCurrent();
        });
    return builder;
  }

  @Override
  public ControlFlowGraphBuilder visit(WithStatementNode node, ControlFlowGraphBuilder builder) {
    build(node.getStatement(), builder);
    builder.nextBlockToCurrent();
    build(node.getTargets(), builder);
    builder.nextBlockToCurrent();
    return builder;
  }

  /**
   * Try statements add handling for exceptions. There is a direct path from the `finally` block to
   * the local exit block. This path would be used when another control flow altering statement is
   * used, such as `Exit`, `Break`, and exceptions. Within try statements, routine invocations gain
   * a successors to the catches/finally block.
   */
  @Override
  public ControlFlowGraphBuilder visit(TryStatementNode node, ControlFlowGraphBuilder builder) {
    builder.nextBlockToCurrent();
    // Finally
    FinallyBlockNode finallyNode = node.getFinallyBlock();
    if (finallyNode != null) {
      builder.nextBlock(
          newBlock().withFinallyPath(builder.getCurrentBlock(), builder.getExitBlock()));
      build(finallyNode.getStatementList(), builder);
      builder.pushLoopContext(builder.getCurrentBlock(), builder.getCurrentBlock());
      builder.pushExitBlock(builder.getCurrentBlock());
    }
    BuilderBlock finallyOrEndBlock = builder.getCurrentBlock();
    BuilderBlock beforeFinally = builder.nextBlockToCurrent();

    // Exception catches
    List<Entry<Type, BuilderBlock>> catches = new ArrayList<>();
    BuilderBlock elseBlock = null;

    ExceptBlockNode exceptBlock = node.getExceptBlock();
    if (exceptBlock != null) {
      if (exceptBlock.isBareExcept()) {
        builder.nextBlockTo(finallyOrEndBlock);
        build(exceptBlock.getStatementList(), builder);
        elseBlock = builder.getCurrentBlock();
      } else if (exceptBlock.getElseBlock() != null) {
        builder.nextBlockTo(finallyOrEndBlock);
        build(exceptBlock.getElseBlock().getStatementList(), builder);
        elseBlock = builder.getCurrentBlock();
      }
      if (exceptBlock.hasHandlers()) {
        for (ExceptItemNode exceptItem : ListUtils.reverse(exceptBlock.getHandlers())) {
          builder.nextBlockTo(finallyOrEndBlock);
          build(exceptItem.getStatement(), builder);
          build(exceptItem.getExceptionName(), builder);
          catches.add(
              0,
              new SimpleEntry<>(
                  exceptItem.getExceptionType().getType(), builder.getCurrentBlock()));
        }
      }
    }

    // Body
    builder.setCurrentBlock(beforeFinally);

    builder.pushTryContext(catches, elseBlock);
    build(node.getStatementList(), builder);
    builder.popTryContext();

    builder.nextBlockToCurrent();
    builder.addElement(node);

    if (finallyNode != null) {
      builder.popExitBlock();
    }
    return builder;
  }

  /**
   * `raise` statements jump directly to the handling exception or exit block. Bare raise statements
   * have successors of all exceptional targets.
   */
  @Override
  public ControlFlowGraphBuilder visit(RaiseStatementNode node, ControlFlowGraphBuilder builder) {
    if (node.getRaiseExpression() == null) {
      Set<BuilderBlock> exceptions = builder.getAllCatchTargets();
      builder.nextBlock(newBlock().withExceptions(builder.getCurrentBlock(), exceptions));
      builder.addElement(node);
      return builder;
    }

    Type raiseType = node.getRaiseExpression().getType();
    BuilderBlock jumpTarget = builder.getCatchTarget(raiseType);
    builder.nextBlock(newBlock().withJump(node, jumpTarget, builder.getCurrentBlock()));
    return build(node.getRaiseExpression(), builder);
  }

  /** Label statements create a new block as they allow for the control flow to jump to them. */
  @Override
  public ControlFlowGraphBuilder visit(LabelStatementNode node, ControlFlowGraphBuilder builder) {
    build(node.getStatement(), builder);
    builder.addLabel(node);
    builder.nextBlockToCurrent();
    return builder;
  }

  /** `goto` statements have a successor of the label they jump to. */
  @Override
  public ControlFlowGraphBuilder visit(GotoStatementNode node, ControlFlowGraphBuilder builder) {
    builder.addGoto(node);
    return builder;
  }

  @Override
  public ControlFlowGraphBuilder visit(
      AssignmentStatementNode node, ControlFlowGraphBuilder builder) {
    build(node.getAssignee(), builder);
    return build(node.getValue(), builder);
  }

  @Override
  public ControlFlowGraphBuilder visit(
      ExpressionStatementNode node, ControlFlowGraphBuilder builder) {
    return build(node.getExpression(), builder);
  }

  // Expressions

  @Override
  public ControlFlowGraphBuilder visit(UnaryExpressionNode node, ControlFlowGraphBuilder builder) {
    builder.addElement(node);
    return build(node.getOperand(), builder);
  }

  @Override
  public ControlFlowGraphBuilder visit(BinaryExpressionNode node, ControlFlowGraphBuilder builder) {
    boolean isBooleanExpr = node.getType().isBoolean();
    if (isBooleanExpr && node.getOperator() == BinaryOperator.AND) {
      return buildBooleanAnd(node, builder);
    } else if (isBooleanExpr && node.getOperator() == BinaryOperator.OR) {
      return buildBooleanOr(node, builder);
    }

    builder.addElement(node);
    build(node.getRight(), builder);
    build(node.getLeft(), builder);
    return builder;
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
  private ControlFlowGraphBuilder buildBooleanAnd(
      BinaryExpressionNode node, ControlFlowGraphBuilder builder) {
    BuilderBlock falseBlock = builder.getCurrentBlock();
    builder.nextBlockTo(falseBlock);
    build(node.getRight(), builder);
    return buildBooleanLHS(builder, node, builder.getCurrentBlock(), falseBlock);
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
  private ControlFlowGraphBuilder buildBooleanOr(
      BinaryExpressionNode node, ControlFlowGraphBuilder builder) {
    BuilderBlock trueBlock = builder.getCurrentBlock();
    builder.nextBlockTo(trueBlock);
    build(node.getRight(), builder);
    return buildBooleanLHS(builder, node, trueBlock, builder.getCurrentBlock());
  }

  private ControlFlowGraphBuilder buildBooleanLHS(
      ControlFlowGraphBuilder builder,
      BinaryExpressionNode node,
      BuilderBlock trueBlock,
      BuilderBlock falseBlock) {
    builder.nextBlock(newBlock().withBranch(node, trueBlock, falseBlock));
    return build(node.getLeft(), builder);
  }

  // Exclusions

  /**
   * Anonymous methods have their own associated control flow graph. One that is separate to the
   * current one being constructed.
   */
  @Override
  public ControlFlowGraphBuilder visit(AnonymousMethodNode node, ControlFlowGraphBuilder builder) {
    return builder;
  }

  /** Assembly control flow graphs are not supported. */
  @Override
  public ControlFlowGraphBuilder visit(AsmStatementNode node, ControlFlowGraphBuilder builder) {
    return builder;
  }

  // Utils

  private ControlFlowGraphBuilder build(DelphiNode node, ControlFlowGraphBuilder builder) {
    if (node == null) {
      return builder;
    }
    return node.accept(this, builder);
  }

  private <T extends DelphiNode> ControlFlowGraphBuilder build(
      List<T> nodes, ControlFlowGraphBuilder builder) {
    ListUtils.reverse(nodes).forEach(node -> build(node, builder));
    return builder;
  }
}
