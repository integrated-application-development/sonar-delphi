package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.operator.BinaryOperator;
import org.sonar.plugins.delphi.symbol.resolve.ExpressionTypeResolver;
import org.sonar.plugins.delphi.type.Type;

public final class BinaryExpressionNode extends ExpressionNode {
  private String image;
  private BinaryOperator operator;

  public BinaryExpressionNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public ExpressionNode getLeft() {
    return (ExpressionNode) jjtGetChild(0);
  }

  public ExpressionNode getRight() {
    return (ExpressionNode) jjtGetChild(1);
  }

  public BinaryOperator getOperator() {
    if (operator == null) {
      operator = BinaryOperator.from(jjtGetId());
    }
    return operator;
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = getLeft().getImage() + " " + getToken().getImage() + " " + getRight().getImage();
    }
    return image;
  }

  @Override
  @NotNull
  public Type createType() {
    return new ExpressionTypeResolver(getTypeFactory()).resolve(this);
  }
}
