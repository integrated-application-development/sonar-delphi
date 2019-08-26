package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class AssignmentStatementNode extends StatementNode {
  public AssignmentStatementNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public ExpressionNode getAssignee() {
    return (ExpressionNode) jjtGetChild(0);
  }

  public ExpressionNode getValue() {
    return (ExpressionNode) jjtGetChild(1);
  }
}
