package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.symbol.resolve.ExpressionTypeResolver;
import org.sonar.plugins.delphi.type.Type;

public final class PrimaryExpressionNode extends ExpressionNode {
  private String image;

  public PrimaryExpressionNode(Token token) {
    super(token);
  }

  public PrimaryExpressionNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public boolean isInheritedCall() {
    return jjtGetChildId(0) == DelphiLexer.INHERITED;
  }

  public boolean isBareInherited() {
    return (jjtGetNumChildren() == 1 || !(jjtGetChild(1) instanceof NameReferenceNode))
        && isInheritedCall();
  }

  @Override
  public String getImage() {
    if (image == null) {
      StringBuilder imageBuilder = new StringBuilder();
      for (int i = 0; i < jjtGetNumChildren(); ++i) {
        imageBuilder.append(jjtGetChild(i).getImage());
      }
      image = imageBuilder.toString();
    }
    return image;
  }

  @Override
  @NotNull
  public Type createType() {
    return new ExpressionTypeResolver(getTypeFactory()).resolve(this);
  }
}
