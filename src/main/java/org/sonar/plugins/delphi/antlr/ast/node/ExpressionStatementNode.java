package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class ExpressionStatementNode extends StatementNode {
  public ExpressionStatementNode(Token token) {
    super(token);
  }

  public ExpressionStatementNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public ExpressionNode getExpression() {
    return (ExpressionNode) jjtGetChild(0);
  }
}
