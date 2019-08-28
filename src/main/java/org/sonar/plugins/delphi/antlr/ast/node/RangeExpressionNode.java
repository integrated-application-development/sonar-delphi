package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;

public final class RangeExpressionNode extends ExpressionNode {
  private String image;

  public RangeExpressionNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @NotNull
  private ExpressionNode getLowExpression() {
    return (ExpressionNode) jjtGetChild(0);
  }

  @NotNull
  private ExpressionNode getHighExpression() {
    return (ExpressionNode) jjtGetChild(1);
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = getLowExpression().getImage() + ".." + getHighExpression().getImage();
    }
    return image;
  }

  @Override
  @NotNull
  public Type createType() {
    Type lowType = getLowExpression().getType();
    if (!lowType.isUnknown()) {
      return lowType;
    }
    return getHighExpression().getType();
  }
}
