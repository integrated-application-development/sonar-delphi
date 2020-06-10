package org.sonar.plugins.delphi.antlr.ast.node;

import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicBoolean.BOOLEAN;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.EXTENDED;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.CHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.UNICODESTRING;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.operator.BinaryOperator;
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
    if (getOperator().isLogicalOperator) {
      return BOOLEAN.type;
    }

    if (getOperator() == BinaryOperator.AS) {
      return getRight().getType();
    }

    Type type = getLeft().getType();

    if (type.is(CHAR.type)) {
      // Assume this expression is a string concatenation
      type = UNICODESTRING.type;
    }

    if (getLeft().getType().isDecimal() || getRight().getType().isDecimal()) {
      // Binary expressions including decimal types always produce an intermediary Extended value
      type = EXTENDED.type;
    }

    return type;
  }

}
