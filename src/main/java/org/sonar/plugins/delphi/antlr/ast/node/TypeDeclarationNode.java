package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class TypeDeclarationNode extends DelphiNode {
  private String qualifiedName;
  private Boolean isSubType;

  public TypeDeclarationNode(Token token) {
    super(token);
  }

  public TypeDeclarationNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public String getSimpleName() {
    return getFirstChildOfType(QualifiedIdentifierNode.class).getSimpleName();
  }

  public String getQualifiedName() {
    if (qualifiedName == null) {
      TypeDeclarationNode node = this;
      StringBuilder name = new StringBuilder();

      while (node != null) {
        var newTypeName = node.getFirstChildOfType(QualifiedIdentifierNode.class);
        if (name.length() != 0) {
          name.insert(0, ".");
        }
        name.insert(0, newTypeName.getQualifiedName());
        node = node.getFirstParentOfType(TypeDeclarationNode.class);
      }
      qualifiedName = name.toString();
    }

    return qualifiedName;
  }

  public TypeNode getTypeDeclaration() {
    return (TypeNode) jjtGetChild(1);
  }

  public QualifiedIdentifierNode getTypeName() {
    return (QualifiedIdentifierNode) jjtGetChild(0);
  }

  public boolean isClass() {
    return getTypeDeclaration() instanceof ClassTypeNode;
  }

  public boolean isClassHelper() {
    return getTypeDeclaration() instanceof ClassHelperTypeNode;
  }

  public boolean isEnum() {
    return getTypeDeclaration() instanceof EnumTypeNode;
  }

  public boolean isInterface() {
    return getTypeDeclaration() instanceof InterfaceTypeNode;
  }

  public boolean isObject() {
    return getTypeDeclaration() instanceof ObjectTypeNode;
  }

  public boolean isRecord() {
    return getTypeDeclaration() instanceof RecordTypeNode;
  }

  public boolean isRecordHelper() {
    return getTypeDeclaration() instanceof RecordHelperTypeNode;
  }

  public boolean isPointer() {
    return getTypeDeclaration() instanceof PointerTypeNode;
  }

  public boolean isSubType() {
    if (isSubType == null) {
      isSubType = getFirstParentOfType(TypeDeclarationNode.class) != null;
    }
    return isSubType;
  }

  public boolean isTypeAlias() {
    return getTypeDeclaration() instanceof TypeAliasNode;
  }

  public boolean isTypeType() {
    return getTypeDeclaration() instanceof TypeTypeNode;
  }
}
