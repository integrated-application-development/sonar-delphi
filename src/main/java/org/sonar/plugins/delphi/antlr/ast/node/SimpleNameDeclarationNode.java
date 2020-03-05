package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class SimpleNameDeclarationNode extends NameDeclarationNode {
  private String image;

  public SimpleNameDeclarationNode(Token token) {
    super(token);
  }

  public SimpleNameDeclarationNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public String getImage() {
    if (image == null) {
      GenericDefinitionNode genericDefinition = getGenericDefinition();
      StringBuilder builder = new StringBuilder();
      builder.append(getIdentifier().getImage());
      if (genericDefinition != null) {
        builder.append(genericDefinition.getImage());
      }
      image = builder.toString();
    }
    return image;
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

  public boolean isPropertyDeclaration() {
    return parent instanceof PropertyNode;
  }

  public boolean isTypeParameterDeclaration() {
    return parent instanceof TypeParameterNode;
  }

  public boolean isVarDeclaration() {
    return getNthParent(2) instanceof VarDeclarationNode;
  }

  public boolean isFieldDeclaration() {
    return getNthParent(2) instanceof FieldDeclarationNode;
  }

  public boolean isFormalParameter() {
    return getNthParent(2) instanceof FormalParameterNode;
  }
}
