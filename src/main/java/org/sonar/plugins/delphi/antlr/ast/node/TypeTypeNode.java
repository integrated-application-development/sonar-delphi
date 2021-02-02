package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;

public final class TypeTypeNode extends TypeNode {
  public TypeTypeNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  private TypeNode getOriginalTypeNode() {
    return (TypeNode) jjtGetChild(0);
  }

  @Override
  @NotNull
  public Type createType() {
    TypeDeclarationNode typeDeclaration = (TypeDeclarationNode) jjtGetParent();
    String typeName = typeDeclaration.fullyQualifiedName();
    return getTypeFactory().typeType(typeName, getOriginalTypeNode().getType());
  }
}
