package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class RecordExpressionItemNode extends DelphiNode {
  public RecordExpressionItemNode(Token token) {
    super(token);
  }

  public RecordExpressionItemNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public IdentifierNode getIdentifier() {
    return (IdentifierNode) jjtGetChild(0);
  }

  public ExpressionNode getExpression() {
    return (ExpressionNode) jjtGetChild(1);
  }

  @Override
  public String getImage() {
    return getIdentifier().getImage() + ": " + getExpression().getImage();
  }
}
