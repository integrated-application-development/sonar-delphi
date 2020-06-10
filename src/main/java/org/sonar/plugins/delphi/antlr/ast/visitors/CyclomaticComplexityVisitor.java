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
