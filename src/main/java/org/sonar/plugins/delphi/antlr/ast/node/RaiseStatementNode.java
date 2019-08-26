package org.sonar.plugins.delphi.antlr.ast.node;

import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class RaiseStatementNode extends StatementNode {
  public RaiseStatementNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Nullable
  public ExpressionNode getRaiseExpression() {
    return (ExpressionNode) jjtGetChild(0);
  }
}
