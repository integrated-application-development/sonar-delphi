package org.sonar.plugins.delphi.antlr.ast.node;

import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicBoolean.BOOLEAN;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.operator.UnaryOperator;
import org.sonar.plugins.delphi.type.DelphiPointerType;
import org.sonar.plugins.delphi.type.Type;

public final class UnaryExpressionNode extends ExpressionNode {
  private UnaryOperator operator;
  private String image;

  public UnaryExpressionNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public UnaryOperator getOperator() {
    if (operator == null) {
      operator = UnaryOperator.from(jjtGetId());
    }
    return operator;
  }

  public ExpressionNode getExpression() {
    return (ExpressionNode) jjtGetChild(0);
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = getToken().getImage() + " " + getExpression().getImage();
    }
    return image;
  }

  @Override
  @NotNull
  public Type createType() {
    Type expressionType = getExpression().getType();
    switch (getOperator()) {
      case NOT:
        return BOOLEAN.type;
      case ADDRESS:
        return DelphiPointerType.pointerTo(expressionType);
      default:
        return expressionType;
    }
  }

}
