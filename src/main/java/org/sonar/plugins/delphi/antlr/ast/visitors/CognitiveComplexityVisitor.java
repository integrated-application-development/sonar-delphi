/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package org.sonar.plugins.delphi.antlr.ast.visitors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.plugins.delphi.antlr.ast.node.AnonymousMethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.BinaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.CaseStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExceptBlockNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.IfStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.RepeatStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.WhileStatementNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.CognitiveComplexityVisitor.Data;
import org.sonar.plugins.delphi.operator.BinaryOperator;

public class CognitiveComplexityVisitor implements DelphiParserVisitor<Data> {
  public static class Data {
    private int complexity;
    private int nesting = 1;
    private final Set<DelphiNode> ignored = new HashSet<>();

    private void increaseComplexityByNesting() {
      updateComplexity(nesting);
    }

    private void increaseComplexityByOne() {
      updateComplexity(1);
    }

    private void updateComplexity(int amount) {
      complexity += amount;
    }

    public int getComplexity() {
      return complexity;
    }
  }

  @Override
  public Data visit(IfStatementNode statement, Data data) {
    data.increaseComplexityByNesting();
    statement.getGuardExpression().accept(this, data);

    ++data.nesting;
    StatementNode thenBranch = statement.getThenStatement();
    if (thenBranch != null) {
      thenBranch.accept(this, data);
    }
    --data.nesting;

    StatementNode elseBranch = statement.getElseStatement();
    if (elseBranch != null) {
      boolean bareElse = !(elseBranch instanceof IfStatementNode);
      if (bareElse) {
        data.increaseComplexityByOne();
        ++data.nesting;
      } else {
        data.updateComplexity(data.nesting - 1);
      }
      elseBranch.accept(this, data);
      if (bareElse) {
        --data.nesting;
      }
    }

    return data;
  }

  @Override
  public Data visit(ExceptBlockNode exceptBlock, Data data) {
    if (exceptBlock.hasHandlers()) {
      exceptBlock.getHandlers().forEach(handler -> data.increaseComplexityByNesting());
      ++data.nesting;
      exceptBlock.getHandlers().forEach(handler -> handler.accept(this, data));
      --data.nesting;
      return data;
    }

    return DelphiParserVisitor.super.visit(exceptBlock, data);
  }

  @Override
  public Data visit(ForStatementNode statement, Data data) {
    data.increaseComplexityByNesting();
    ++data.nesting;
    DelphiParserVisitor.super.visit(statement, data);
    --data.nesting;
    return data;
  }

  @Override
  public Data visit(WhileStatementNode statement, Data data) {
    data.increaseComplexityByNesting();
    ++data.nesting;
    DelphiParserVisitor.super.visit(statement, data);
    --data.nesting;
    return data;
  }

  @Override
  public Data visit(RepeatStatementNode statement, Data data) {
    data.increaseComplexityByNesting();
    ++data.nesting;
    DelphiParserVisitor.super.visit(statement, data);
    --data.nesting;
    return data;
  }

  @Override
  public Data visit(CaseStatementNode statement, Data data) {
    data.increaseComplexityByNesting();
    ++data.nesting;
    DelphiParserVisitor.super.visit(statement, data);
    --data.nesting;
    return data;
  }

  @Override
  public Data visit(AnonymousMethodNode method, Data data) {
    ++data.nesting;
    DelphiParserVisitor.super.visit(method, data);
    --data.nesting;
    return data;
  }

  @Override
  public Data visit(BinaryExpressionNode expression, Data data) {
    if (isAndOrExpression(expression) && !data.ignored.contains(expression)) {
      List<BinaryExpressionNode> flattenedLogicalExpressions =
          flattenLogicalExpression(expression, data).collect(Collectors.toList());

      BinaryExpressionNode previous = null;
      for (BinaryExpressionNode current : flattenedLogicalExpressions) {
        if (previous == null || previous.getOperator() != current.getOperator()) {
          data.increaseComplexityByOne();
        }
        previous = current;
      }
    }
    return DelphiParserVisitor.super.visit(expression, data);
  }

  private Stream<BinaryExpressionNode> flattenLogicalExpression(
      ExpressionNode expression, Data data) {
    if (isAndOrExpression(expression)) {
      data.ignored.add(expression);

      BinaryExpressionNode binaryExpr = (BinaryExpressionNode) expression;
      ExpressionNode left = binaryExpr.getLeft().skipParentheses();
      ExpressionNode right = binaryExpr.getRight().skipParentheses();

      return Stream.concat(
          Stream.concat(flattenLogicalExpression(left, data), Stream.of(binaryExpr)),
          flattenLogicalExpression(right, data));
    }
    return Stream.empty();
  }

  private static boolean isAndOrExpression(ExpressionNode expression) {
    if (expression instanceof BinaryExpressionNode) {
      BinaryExpressionNode binaryExpression = (BinaryExpressionNode) expression;
      BinaryOperator operator = binaryExpression.getOperator();
      return operator == BinaryOperator.AND || operator == BinaryOperator.OR;
    }
    return false;
  }
}
