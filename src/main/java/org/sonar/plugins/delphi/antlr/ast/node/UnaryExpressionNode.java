package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.operator.UnaryOperator;
import org.sonar.plugins.delphi.symbol.resolve.ExpressionTypeUtils;
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

  public ExpressionNode getOperand() {
    return (ExpressionNode) jjtGetChild(0);
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = getToken().getImage() + " " + getOperand().getImage();
    }
    return image;
  }

  @Override
  @NotNull
  public Type createType() {
    return ExpressionTypeUtils.resolve(this);
  }
}
