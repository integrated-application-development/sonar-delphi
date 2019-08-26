package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class EnumTypeNode extends TypeNode {
  private String image;

  public EnumTypeNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public String getImage() {
    if (image == null) {
      StringBuilder imageBuilder = new StringBuilder();
      imageBuilder.append("(");
      for (EnumTypeElementNode element : findChildrenOfType(EnumTypeElementNode.class)) {
        imageBuilder.append(element.getImage());
      }
      imageBuilder.append(")");
      image = imageBuilder.toString();
    }
    return image;
  }
}
