package org.sonar.plugins.delphi.antlr.ast.node;

import static org.sonar.plugins.delphi.type.TypeUtils.ordinalSize;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.DelphiSubrangeType;
import org.sonar.plugins.delphi.type.Type;

public final class SubRangeTypeNode extends TypeNode {
  public SubRangeTypeNode(Token token) {
    super(token);
  }

  public SubRangeTypeNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public ExpressionNode getLowExpression() {
    return (ExpressionNode) jjtGetChild(0);
  }

  public ExpressionNode getHighExpression() {
    return (ExpressionNode) jjtGetChild(1);
  }

  @Override
  @NotNull
  public Type createType() {
    ExpressionNode low = getLowExpression();
    ExpressionNode high = getHighExpression();
    Type baseType;

    if (ordinalSize(low.getType()) > ordinalSize(high.getType())) {
      baseType = low.getType();
    } else {
      baseType = high.getType();
    }

    return DelphiSubrangeType.subRange(
        "Subrange(" + low.getImage() + ".." + high.getImage() + ")", baseType);
  }
}
