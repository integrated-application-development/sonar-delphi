package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

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
    return jjtGetNumChildren() == 1 && isInheritedCall();
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
}
