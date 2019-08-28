package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.DelphiCollectionType;
import org.sonar.plugins.delphi.type.Type;

public final class SetTypeNode extends TypeNode {
  public SetTypeNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  private TypeNode getElementTypeNode() {
    return (TypeNode) jjtGetChild(1);
  }

  @NotNull
  @Override
  public Type createType() {
    return DelphiCollectionType.set(getElementTypeNode().getType());
  }
}
