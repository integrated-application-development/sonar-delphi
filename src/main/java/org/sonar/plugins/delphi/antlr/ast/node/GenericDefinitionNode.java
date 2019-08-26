package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class GenericDefinitionNode extends DelphiNode {
  private String image;

  public GenericDefinitionNode(Token token) {
    super(token);
  }

  public GenericDefinitionNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
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
