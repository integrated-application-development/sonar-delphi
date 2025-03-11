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

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import au.com.integradev.delphi.cfg.block.ProtoBlock;
import au.com.integradev.delphi.cfg.block.ProtoBlockFactory;
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

/**
 * This visitor populates the {@link ControlFlowGraphBuilder} to construct a control flow graph.
 * Generally, the statements and elements are traversed backward simplify the construction of a
 * directed graph. `Block` objects are typically are ordered in way they are evaluated.
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
  public ControlFlowGraphBuilder visit(TextLiteralNode node, ControlFlowGraphBuilder builder) {
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
    build(node.getHighExpression(), builder);
    return build(node.getLowExpression(), builder);
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

  /*
   * NameReferenceNode has overloaded meanings in Delphi. The control flow intrinsics are
   * handled individually.
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
        builder.addBlock(
            ProtoBlockFactory.jump(node, builder.getExitBlock(), builder.getCurrentBlock()));
        return builder;
      case "System.Break":
        return buildControlFlowStatement(node, builder.getBreakTarget(), builder);
      case "System.Halt":
        builder.addBlock(ProtoBlockFactory.halt(node));
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

  private static ControlFlowGraphBuilder buildControlFlowStatement(
      DelphiNode node, ProtoBlock target, ControlFlowGraphBuilder builder) {
    if (target == null) {
      throw new IllegalStateException(
          String.format("'%s' statement not in loop statement", node.getImage()));
    }
    builder.addBlock(ProtoBlockFactory.jump(node, target, builder.getCurrentBlock()));
    return builder;
  }

  private static void handleExceptionalPaths(ControlFlowGraphBuilder builder) {
    if (!builder.inTryContext()) {
      // Only consider routines as potentially exceptional if in a `try` context.
      return;
    }

    Set<ProtoBlock> exceptions = builder.getAllCatchTargets();
    builder.addBlock(ProtoBlockFactory.withExceptions(builder.getCurrentBlock(), exceptions));
  }

  // Overridden to ensure NodeDeclarations are added in the correct order
  @Override
  public ControlFlowGraphBuilder visit(
      NameDeclarationListNode node, ControlFlowGraphBuilder builder) {
    return build(node.getDeclarations(), builder);
  }

  // ExpressionNode covers `inherited` expressions
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
    builder.addElement(node.skipParentheses().getChild(0));
    return builder;
  }

  // Statements

  @Override
  public ControlFlowGraphBuilder visit(StatementListNode node, ControlFlowGraphBuilder builder) {
    return build(node.getStatements(), builder);
  }

  /*
   * From the condition there are two successors, a true branch and a false branch. Both branches
   * will have successors of the block that comes after.
   *
   * `if Condition then ThenBlock else ElseBlock;` maps to
   *
   *              ┌─> true ──> `ThenBlock` ─┐
   * `Condition` ─┤                         ├─> after
   *              └─> false ─> `ElseBlock` ─┘
   */
  @Override
  public ControlFlowGraphBuilder visit(IfStatementNode node, ControlFlowGraphBuilder builder) {
    ProtoBlock after = builder.getCurrentBlock();

    // process `else`
    builder.addBlockBefore(after);
    build(node.getElseStatement(), builder);
    ProtoBlock elseBlock = builder.getCurrentBlock();

    // process `then`
    builder.addBlockBefore(after);
    build(node.getThenStatement(), builder);
    ProtoBlock thenBlock = builder.getCurrentBlock();

    // process condition
    builder.addBlock(ProtoBlockFactory.branch(node, thenBlock, elseBlock));
    return buildCondition(builder, node.getGuardExpression(), thenBlock, elseBlock);
  }

  private ControlFlowGraphBuilder buildCondition(
      ControlFlowGraphBuilder builder,
      ExpressionNode node,
      ProtoBlock trueBlock,
      ProtoBlock falseBlock) {
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
      ProtoBlock trueBlock,
      ProtoBlock falseBlock) {
    // RHS
    buildCondition(builder, node.getRight(), trueBlock, falseBlock);
    ProtoBlock newTrueBlock = builder.getCurrentBlock();
    // LHS
    builder.addBlock(ProtoBlockFactory.branch(node, newTrueBlock, falseBlock));
    return buildCondition(builder, node.getLeft(), newTrueBlock, falseBlock);
  }

  private ControlFlowGraphBuilder buildConditionOr(
      ControlFlowGraphBuilder builder,
      BinaryExpressionNode node,
      ProtoBlock trueBlock,
      ProtoBlock falseBlock) {
    // RHS
    buildCondition(builder, node.getRight(), trueBlock, falseBlock);
    ProtoBlock newFalseBlock = builder.getCurrentBlock();
    // LHS
    builder.addBlock(ProtoBlockFactory.branch(node, trueBlock, newFalseBlock));
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

  /*
   * The case's selector expression has a successors of each case, and the optional else block.
   *
   * case Selector of
   *   A: Body1;
   *   B: Body2;
   * else
   *   Body3;
   * end;
   *
   * maps to
   *             ┌─>   `A`  => `Body1` ─┐
   * `Selector` ─┼─>   `B`  => `Body2` ─┼─> after
   *             └─> `else` => `Body3` ─┘
   */
  @Override
  public ControlFlowGraphBuilder visit(CaseStatementNode node, ControlFlowGraphBuilder builder) {
    ProtoBlock after = builder.getCurrentBlock();

    // Selector
    ProtoBlock caseBlock = builder.addBlockBeforeCurrent();

    List<ExpressionNode> caseLabels =
        node.getCaseItems().stream()
            .flatMap(caseNode -> caseNode.getExpressions().stream())
            .collect(Collectors.toList());
    build(caseLabels, builder);
    build(node.getSelectorExpression(), builder);
    ProtoBlock conditionBlock = builder.getCurrentBlock();

    Set<ProtoBlock> caseSuccessors = new HashSet<>();

    // If none of the case arms match, it will either skip the block, or
    // fallthrough to an `else` block
    ProtoBlock fallthrough = after;
    // Else
    if (node.getElseBlockNode() != null) {
      builder.addBlockBefore(after);
      build(node.getElseBlockNode().getStatementList(), builder);
      fallthrough = builder.getCurrentBlock();
    }

    // Cases
    for (StatementNode statement :
        ListUtils.reverse(
            node.getCaseItems().stream()
                .map(CaseItemStatementNode::getStatement)
                .collect(Collectors.toList()))) {
      builder.addBlockBefore(after);
      build(statement, builder);
      caseSuccessors.add(builder.getCurrentBlock());
    }

    caseBlock.update(ProtoBlockFactory.cases(node, caseSuccessors, fallthrough));
    builder.setCurrentBlock(conditionBlock);
    return builder;
  }

  /*
   * Repeat statements flow through the body first, then consult the condition as to where to go
   * next.
   *
   * repeat
   *   Body;
   * until Condition;
   *
   * maps to
   *                        ┌─> true ────┐
   * `Body` ─> `Condition` ─┴─> false ─┐ └─> after
   *   ^───────────────────────────────┘
   */
  @Override
  public ControlFlowGraphBuilder visit(RepeatStatementNode node, ControlFlowGraphBuilder builder) {
    ProtoBlock after = builder.getCurrentBlock();
    // Create a placeholder for the body's starting block
    ProtoBlock body = builder.addBlockBeforeCurrent();

    // Condition
    builder.addBlock(ProtoBlockFactory.branch(node, after, body));
    buildCondition(builder, node.getGuardExpression(), after, body);

    // Body
    builder.pushLoopContext(builder.getCurrentBlock(), after);
    builder.addBlockBeforeCurrent();
    build(node.getStatementList(), builder);
    builder.popLoopContext();

    body.update(ProtoBlockFactory.linear(builder.getCurrentBlock()));
    builder.addBlockBeforeCurrent();
    return builder;
  }

  /*
   * While loops flow through the condition first, then based on its value either enter the loop or
   * continue on.
   *
   * `while Condition do Body;` maps to
   *
   *              ┌─> false ─────────────┐
   * `Condition` ─┴─> true ──> `Body` ─┐ └─> after
   *     ^─────────────────────────────┘
   */
  @Override
  public ControlFlowGraphBuilder visit(WhileStatementNode node, ControlFlowGraphBuilder builder) {
    ProtoBlock after = builder.getCurrentBlock();
    // Create a placeholder for the condition's starting block
    ProtoBlock condition = builder.addBlockBeforeCurrent();

    // Body
    builder.addBlockBefore(condition);
    builder.pushLoopContext(condition, after);
    build(node.getStatement(), builder);
    builder.popLoopContext();
    ProtoBlock body = builder.getCurrentBlock();

    // Condition
    builder.setCurrentBlock(condition);
    builder.addBlock(ProtoBlockFactory.branch(node, body, after));
    buildCondition(builder, node.getGuardExpression(), body, after);

    condition.update(ProtoBlockFactory.linear(builder.getCurrentBlock()));
    builder.addBlockBeforeCurrent();
    return builder;
  }

  /*
   * For to/downto loops evaluate in order the low value expression, the high expression, the body,
   * and the variable. The variable has branching behaviour based on whether there is a next element
   * in the range.
   *
   * `for A := B to C do Body;` maps to
   *
   * `B` ─> `C` ─> `Body` ─> `A` ─┬─> false ─> after
   *                 ^─── true <──┘
   */
  @Override
  public ControlFlowGraphBuilder visit(ForToStatementNode node, ControlFlowGraphBuilder builder) {
    return buildForLoop(
        node,
        List.of(node.getVariable(), node.getTargetExpression(), node.getInitializerExpression()),
        builder);
  }

  /*
   * For in loops evaluate in order the enumerable, the body, and the variable. The variable has
   * branching behaviour based on whether there is a next element in the enumerable.
   *
   * `for A in B do Body;` maps to
   *
   * `B` ─> `Body` ─> `A` ─┬─> false ─> after
   *          ^─── true <──┘
   */
  @Override
  public ControlFlowGraphBuilder visit(ForInStatementNode node, ControlFlowGraphBuilder builder) {
    return buildForLoop(node, List.of(node.getVariable(), node.getEnumerable()), builder);
  }

  private ControlFlowGraphBuilder buildForLoop(
      ForStatementNode node, List<DelphiNode> parts, ControlFlowGraphBuilder builder) {
    ProtoBlock after = builder.getCurrentBlock();
    // Create a placeholder for the conditional block
    ProtoBlock loopback = builder.addBlockBeforeCurrent();
    builder.addBlockBeforeCurrent();

    builder.pushLoopContext(loopback, after);
    build(node.getStatement(), builder);
    builder.popLoopContext();

    loopback.update(ProtoBlockFactory.branch(node, builder.getCurrentBlock(), after));

    builder.setCurrentBlock(loopback);
    parts.forEach(
        part -> {
          build(part, builder);
          builder.addBlockBeforeCurrent();
        });
    return builder;
  }

  @Override
  public ControlFlowGraphBuilder visit(WithStatementNode node, ControlFlowGraphBuilder builder) {
    builder.addBlockBeforeCurrent();
    build(node.getStatement(), builder);
    builder.addBlockBeforeCurrent();
    build(node.getTargets(), builder);
    builder.addBlockBeforeCurrent();
    return builder;
  }

  /*
   * Try statements add handling for exceptions. There is a direct path from the `finally` block to
   * the local exit block. This path would be used when another control flow altering statement is
   * used, such as `Exit`, `Break`, and exceptions. Within `try` statements, routine invocations
   * gain a successor to the catches/finally block.
   */
  @Override
  public ControlFlowGraphBuilder visit(TryStatementNode node, ControlFlowGraphBuilder builder) {
    if (node.getFinallyBlock() != null) {
      return buildTryFinally(node, builder);
    } else {
      return buildTryExcept(node, builder);
    }
  }

  private ControlFlowGraphBuilder buildTryFinally(
      TryStatementNode node, ControlFlowGraphBuilder builder) {
    // to ensure after a `finally` exceptional paths still exist
    handleExceptionalPaths(builder);

    builder.addBlockBeforeCurrent();
    // Finally
    FinallyBlockNode finallyNode = node.getFinallyBlock();
    builder.addBlock(
        ProtoBlockFactory.finallyBlock(builder.getCurrentBlock(), builder.getExitBlock()));
    build(finallyNode.getStatementList(), builder);
    builder.pushLoopContext(builder.getCurrentBlock(), builder.getCurrentBlock());
    builder.pushExitBlock(builder.getCurrentBlock());

    builder.addBlockBeforeCurrent();

    // Body
    builder.pushTryFinallyContext();
    build(node.getStatementList(), builder);
    builder.popTryContext();

    builder.addBlockBeforeCurrent();
    builder.addElement(node);

    builder.popExitBlock();
    return builder;
  }

  private ControlFlowGraphBuilder buildTryExcept(
      TryStatementNode node, ControlFlowGraphBuilder builder) {
    ProtoBlock endBlock = builder.getCurrentBlock();
    ProtoBlock beforeEnd = builder.addBlockBeforeCurrent();

    // Exception catches
    List<Entry<Type, ProtoBlock>> catches = new ArrayList<>();
    ProtoBlock elseBlock = null;

    ExceptBlockNode exceptBlock = node.getExceptBlock();
    if (exceptBlock.isBareExcept()) {
      builder.addBlockBefore(endBlock);
      build(exceptBlock.getStatementList(), builder);
      elseBlock = builder.getCurrentBlock();
    } else if (exceptBlock.getElseBlock() != null) {
      builder.addBlockBefore(endBlock);
      build(exceptBlock.getElseBlock().getStatementList(), builder);
      elseBlock = builder.getCurrentBlock();
    }
    if (exceptBlock.hasHandlers()) {
      for (ExceptItemNode exceptItem : ListUtils.reverse(exceptBlock.getHandlers())) {
        builder.addBlockBefore(endBlock);
        build(exceptItem.getStatement(), builder);
        build(exceptItem.getExceptionName(), builder);
        catches.add(
            0,
            new SimpleEntry<>(exceptItem.getExceptionType().getType(), builder.getCurrentBlock()));
      }
    }

    // Body
    builder.setCurrentBlock(beforeEnd);

    builder.pushTryExceptContext(catches, elseBlock);
    build(node.getStatementList(), builder);
    builder.popTryContext();

    builder.addBlockBeforeCurrent();
    builder.addElement(node);

    return builder;
  }

  /*
   * `raise` statements jump directly to the handling exception or exit block. Bare raise statements
   * have successors of all exceptional targets.
   */
  @Override
  public ControlFlowGraphBuilder visit(RaiseStatementNode node, ControlFlowGraphBuilder builder) {
    if (node.getRaiseExpression() == null) {
      Set<ProtoBlock> exceptions = builder.getAllCatchTargets();
      builder.addBlock(ProtoBlockFactory.withExceptions(builder.getCurrentBlock(), exceptions));
      builder.addElement(node);
      return builder;
    }

    Type raiseType = node.getRaiseExpression().getType();
    ProtoBlock jumpTarget = builder.getCatchTarget(raiseType);
    builder.addBlock(ProtoBlockFactory.jump(node, jumpTarget, builder.getCurrentBlock()));
    return build(node.getRaiseExpression(), builder);
  }

  // Label statements create a new block as they allow for the control flow to jump to them.
  @Override
  public ControlFlowGraphBuilder visit(LabelStatementNode node, ControlFlowGraphBuilder builder) {
    build(node.getStatement(), builder);
    builder.addLabel(node);
    builder.addBlockBeforeCurrent();
    return builder;
  }

  // `goto` statements have a successor of the label they jump to.
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

  /*
   * Boolean `and` expressions represent the short-circuiting behaviour. `A and B` is as follows:
   *
   *      ┌─> true ─> `B` ─┐
   * `A` ─┤                ├─> after
   *      └─> false ───────┘
   */
  private ControlFlowGraphBuilder buildBooleanAnd(
      BinaryExpressionNode node, ControlFlowGraphBuilder builder) {
    ProtoBlock falseBlock = builder.getCurrentBlock();
    builder.addBlockBefore(falseBlock);
    build(node.getRight(), builder);
    return buildBooleanLHS(builder, node, builder.getCurrentBlock(), falseBlock);
  }

  /*
   * Boolean `or` expressions represent the short-circuiting behaviour. `A or B` is as follows:
   *
   *      ┌─> true ─────────┐
   * `A` ─┤                 ├─> after
   *      └─> false ─> `B` ─┘
   */
  private ControlFlowGraphBuilder buildBooleanOr(
      BinaryExpressionNode node, ControlFlowGraphBuilder builder) {
    ProtoBlock trueBlock = builder.getCurrentBlock();
    builder.addBlockBefore(trueBlock);
    build(node.getRight(), builder);
    return buildBooleanLHS(builder, node, trueBlock, builder.getCurrentBlock());
  }

  private ControlFlowGraphBuilder buildBooleanLHS(
      ControlFlowGraphBuilder builder,
      BinaryExpressionNode node,
      ProtoBlock trueBlock,
      ProtoBlock falseBlock) {
    builder.addBlock(ProtoBlockFactory.branch(node, trueBlock, falseBlock));
    return build(node.getLeft(), builder);
  }

  // Exclusions

  /*
   * Anonymous methods have their own associated control flow graph. One that is separate to the
   * current one being constructed.
   */
  @Override
  public ControlFlowGraphBuilder visit(AnonymousMethodNode node, ControlFlowGraphBuilder builder) {
    return builder;
  }

  // Assembly control flow graphs are not supported.
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
