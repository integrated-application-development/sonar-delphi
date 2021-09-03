package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public final class VarDeclarationNode extends DelphiNode implements Typed {
  public VarDeclarationNode(Token token) {
    super(token);
  }

  public VarDeclarationNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public NameDeclarationListNode getNameDeclarationList() {
    return (NameDeclarationListNode) jjtGetChild(0);
  }

  public VarSectionNode getVarSection() {
    return (VarSectionNode) jjtGetParent();
  }

  public TypeNode getTypeNode() {
    return (TypeNode) jjtGetChild(1);
  }

  public boolean isAbsolute() {
    return getFirstChildWithId(DelphiLexer.ABSOLUTE) != null;
  }

  @NotNull
  @Override
  public Type getType() {
    return getTypeNode().getType();
  }
}
