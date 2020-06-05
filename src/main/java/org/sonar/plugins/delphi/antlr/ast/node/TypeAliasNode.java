package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;

public final class TypeAliasNode extends TypeNode {
  public TypeAliasNode(Token token) {
    super(token);
  }

  public TypeAliasNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public TypeReferenceNode getAliasedTypeNode() {
    return (TypeReferenceNode) jjtGetChild(0);
  }

  @NotNull
  @Override
  public Type createType() {
    return getAliasedTypeNode().getType();
  }
}
