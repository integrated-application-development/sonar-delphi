package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Typed;

public final class VarNameDeclarationNode extends NameDeclarationNode {

  public VarNameDeclarationNode(Token token) {
    super(token);
  }

  public VarNameDeclarationNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public String getImage() {
    return getIdentifier().getImage();
  }

  public IdentifierNode getIdentifier() {
    return (IdentifierNode) jjtGetChild(0);
  }

  public boolean isConstDeclaration() {
    return parent instanceof ConstDeclarationNode;
  }

  public boolean isExceptItemDeclaration() {
    return parent instanceof ExceptItemNode;
  }

  public boolean isVarDeclaration() {
    return getNthParent(2) instanceof VarDeclarationNode;
  }

  public boolean isFieldDeclaration() {
    return getNthParent(2) instanceof FieldSectionNode;
  }

  public boolean isFormalParameter() {
    return getNthParent(2) instanceof FormalParameterNode;
  }

  public Typed getTypedDeclaration() {
    if (isConstDeclaration() || isExceptItemDeclaration()) {
      return ((Typed) parent);
    } else {
      return ((Typed) getNthParent(2));
    }
  }
}
