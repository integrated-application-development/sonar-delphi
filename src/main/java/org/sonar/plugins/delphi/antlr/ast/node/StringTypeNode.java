package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicType;

public final class StringTypeNode extends TypeNode {
  public StringTypeNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  private boolean isFixedString() {
    return jjtGetChild(0) instanceof ExpressionNode;
  }

  @Override
  @NotNull
  public Type createType() {
    IntrinsicType intrinsic = isFixedString() ? IntrinsicType.SHORTSTRING : IntrinsicType.STRING;
    return getTypeFactory().getIntrinsic(intrinsic);
  }
}
