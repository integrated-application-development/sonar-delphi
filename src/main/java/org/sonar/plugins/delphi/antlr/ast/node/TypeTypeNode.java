package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class TypeTypeNode extends TypeNode {
  public TypeTypeNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public QualifiedIdentifierNode getOriginalTypeIdentifier() {
    return (QualifiedIdentifierNode) jjtGetChild(0);
  }

  @Override
  public String getImage() {
    return "type " + getOriginalTypeIdentifier().getImage();
  }
}
