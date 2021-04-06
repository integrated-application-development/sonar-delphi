package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class ForToStatementNode extends ForStatementNode {
  public ForToStatementNode(Token token) {
    super(token);
  }

  public ExpressionNode getInitializerExpression() {
    return (ExpressionNode) jjtGetChild(2);
  }

  public ExpressionNode getTargetExpression() {
    return (ExpressionNode) jjtGetChild(4);
  }

  @Override
  public StatementNode getStatement() {
    return (StatementNode) jjtGetChild(6);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }
}
