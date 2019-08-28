package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.DelphiPointerType;
import org.sonar.plugins.delphi.type.Type;

public final class PointerTypeNode extends TypeNode {
  public PointerTypeNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  private TypeNode getTypeNode() {
    return (TypeNode) jjtGetChild(0);
  }

  @NotNull
  @Override
  public Type createType() {
    return DelphiPointerType.pointerTo(getTypeNode().getType());
  }
}
