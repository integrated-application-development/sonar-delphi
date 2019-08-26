package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class EnumTypeElementNode extends DelphiNode {
  public EnumTypeElementNode(Token token) {
    super(token);
  }

  public EnumTypeElementNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public IdentifierNode getIdentifier() {
    return (IdentifierNode) jjtGetChild(0);
  }

  @Override
  public String getImage() {
    return getIdentifier().getImage();
  }
}
