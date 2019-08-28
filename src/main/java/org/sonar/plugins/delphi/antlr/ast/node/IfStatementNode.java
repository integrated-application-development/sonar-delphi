package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class IfStatementNode extends StatementNode {
  public IfStatementNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public ExpressionNode getGuardExpression() {
    return (ExpressionNode) jjtGetChild(0);
  }

  @Nullable
  public StatementNode getThenBranch() {
    return (StatementNode) jjtGetChild(2);
  }

  @Nullable
  public StatementNode getElseBranch() {
    return (StatementNode) jjtGetChild(4);
  }
}
