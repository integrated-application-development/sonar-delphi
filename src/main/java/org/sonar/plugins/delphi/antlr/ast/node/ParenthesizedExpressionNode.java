package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;

public final class ParenthesizedExpressionNode extends ExpressionNode {
  private String image;

  public ParenthesizedExpressionNode(Token token) {
    super(token);
  }

  public ParenthesizedExpressionNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public ExpressionNode getExpression() {
    return (ExpressionNode) jjtGetChild(1);
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = "(" + getExpression().getImage() + ")";
    }
    return image;
  }

  @Override
  @NotNull
  public Type createType() {
    return getExpression().getType();
  }
}
