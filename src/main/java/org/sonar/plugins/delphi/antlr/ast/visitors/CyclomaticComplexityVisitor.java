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

import org.sonar.plugins.delphi.antlr.ast.node.AnonymousMethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.BinaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.CaseItemStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.IfStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.RepeatStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.WhileStatementNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.CyclomaticComplexityVisitor.Data;
import org.sonar.plugins.delphi.operator.BinaryOperator;

public class CyclomaticComplexityVisitor implements DelphiParserVisitor<Data> {
  public static class Data {
    private int complexity;

    public int getComplexity() {
      return complexity;
    }
  }

  @Override
  public Data visit(MethodImplementationNode method, Data data) {
    ++data.complexity;
    return DelphiParserVisitor.super.visit(method, data);
  }

  @Override
  public Data visit(AnonymousMethodNode method, Data data) {
    ++data.complexity;
    return DelphiParserVisitor.super.visit(method, data);
  }

  @Override
  public Data visit(CaseItemStatementNode statement, Data data) {
    ++data.complexity;
    return DelphiParserVisitor.super.visit(statement, data);
  }

  @Override
  public Data visit(ForStatementNode statement, Data data) {
    ++data.complexity;
    return DelphiParserVisitor.super.visit(statement, data);
  }

  @Override
  public Data visit(WhileStatementNode statement, Data data) {
    ++data.complexity;
    return DelphiParserVisitor.super.visit(statement, data);
  }

  @Override
  public Data visit(RepeatStatementNode statement, Data data) {
    ++data.complexity;
    return DelphiParserVisitor.super.visit(statement, data);
  }

  @Override
  public Data visit(IfStatementNode statement, Data data) {
    ++data.complexity;
    return DelphiParserVisitor.super.visit(statement, data);
  }

  @Override
  public Data visit(BinaryExpressionNode expression, Data data) {
    BinaryOperator operator = expression.getOperator();
    if (operator == BinaryOperator.AND || operator == BinaryOperator.OR) {
      ++data.complexity;
    }
    return DelphiParserVisitor.super.visit(expression, data);
  }
}
