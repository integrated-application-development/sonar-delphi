package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class RangeNode extends DelphiNode {
  private String image;

  public RangeNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public ExpressionNode getLow() {
    return (ExpressionNode) jjtGetChild(0);
  }

  public ExpressionNode getHigh() {
    return (ExpressionNode) jjtGetChild(1);
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = getLow().getImage() + ".." + getHigh().getImage();
    }
    return image;
  }
}
