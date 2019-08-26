package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class ClassHelperTypeNode extends TypeNode {
  public ClassHelperTypeNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public QualifiedIdentifierNode getFor() {
    return getFirstChildOfType(QualifiedIdentifierNode.class);
  }

  @Override
  public String getImage() {
    return "class helper for " + getFor().getImage();
  }
}
