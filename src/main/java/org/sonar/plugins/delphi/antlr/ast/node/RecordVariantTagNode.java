package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public final class RecordVariantTagNode extends DelphiNode implements Typed {
  public RecordVariantTagNode(Token token) {
    super(token);
  }

  public RecordVariantTagNode(int tokenType) {
    super(tokenType);
  }

  private boolean hasTagName() {
    return jjtGetChild(0) instanceof NameDeclarationNode;
  }

  public TypeNode getTypeNode() {
    return (TypeNode) jjtGetChild(hasTagName() ? 1 : 0);
  }

  @NotNull
  @Override
  public Type getType() {
    return getTypeNode().getType();
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }
}
