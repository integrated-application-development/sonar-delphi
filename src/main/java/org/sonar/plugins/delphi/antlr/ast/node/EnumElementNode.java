package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public final class EnumElementNode extends DelphiNode implements Typed {
  public EnumElementNode(Token token) {
    super(token);
  }

  public EnumElementNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public QualifiedNameDeclarationNode getNameDeclarationNode() {
    return (QualifiedNameDeclarationNode) jjtGetChild(0);
  }

  @Override
  public String getImage() {
    return getNameDeclarationNode().getImage();
  }

  @NotNull
  @Override
  public Type getType() {
    return ((EnumTypeNode) parent).getType();
  }
}
