/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import au.com.integradev.delphi.symbol.Qualifiable;
import au.com.integradev.delphi.symbol.QualifiedName;
import au.com.integradev.delphi.symbol.declaration.TypeNameDeclaration;
import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Typed;
import java.util.ArrayDeque;
import java.util.Deque;
import org.antlr.runtime.Token;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TypeDeclarationNode extends DelphiNode implements Typed, Qualifiable {
  private Boolean isSubType;
  private QualifiedName qualifiedName;
  private String qualifiedNameExcludingUnit;

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

  @Override
  public String getImage() {
    return fullyQualifiedName();
  }

  public SimpleNameDeclarationNode getTypeNameNode() {
    return (SimpleNameDeclarationNode) jjtGetChild(0);
  }

  public TypeNode getTypeNode() {
    return (TypeNode) jjtGetChild(1);
  }

  @Nullable
  public TypeNameDeclaration getTypeNameDeclaration() {
    return (TypeNameDeclaration) getTypeNameNode().getNameDeclaration();
  }

  @Override
  public String simpleName() {
    return getTypeNameNode().getIdentifier().getImage();
  }

  @Override
  public QualifiedName getQualifiedName() {
    if (qualifiedName == null) {
      buildQualifiedNames();
    }

    return qualifiedName;
  }

  public String qualifiedNameExcludingUnit() {
    if (qualifiedNameExcludingUnit == null) {
      buildQualifiedNames();
    }
    return qualifiedNameExcludingUnit;
  }

  private void buildQualifiedNames() {
    TypeDeclarationNode node = this;
    Deque<String> names = new ArrayDeque<>();

    while (node != null) {
      names.addFirst(node.getTypeNameNode().getImage());
      node = node.getFirstParentOfType(TypeDeclarationNode.class);
    }

    this.qualifiedNameExcludingUnit = StringUtils.join(names, ".");
    names.addFirst(findUnitName());

    qualifiedName = new QualifiedName(names);
  }

  public Deque<TypeDeclarationNode> getOuterTypeDeclarationNodes() {
    Deque<TypeDeclarationNode> result = new ArrayDeque<>();
    TypeDeclarationNode node = this;
    while (true) {
      node = node.getFirstParentOfType(TypeDeclarationNode.class);
      if (node == null) {
        break;
      }
      result.addFirst(node);
    }
    return result;
  }

  public boolean isClass() {
    return getTypeNode() instanceof ClassTypeNode;
  }

  public boolean isClassHelper() {
    return getTypeNode() instanceof ClassHelperTypeNode;
  }

  public boolean isClassReference() {
    return getTypeNode() instanceof ClassReferenceTypeNode;
  }

  public boolean isEnum() {
    return getTypeNode() instanceof EnumTypeNode;
  }

  public boolean isInterface() {
    return getTypeNode() instanceof InterfaceTypeNode;
  }

  public boolean isObject() {
    return getTypeNode() instanceof ObjectTypeNode;
  }

  public boolean isRecord() {
    return getTypeNode() instanceof RecordTypeNode;
  }

  public boolean isRecordHelper() {
    return getTypeNode() instanceof RecordHelperTypeNode;
  }

  public boolean isPointer() {
    return getTypeNode() instanceof PointerTypeNode;
  }

  public boolean isNestedType() {
    if (isSubType == null) {
      isSubType = getFirstParentOfType(TypeDeclarationNode.class) != null;
    }
    return isSubType;
  }

  public boolean isTypeAlias() {
    return getTypeNode() instanceof TypeAliasNode;
  }

  public boolean isTypeType() {
    return getTypeNode() instanceof TypeTypeNode;
  }

  public boolean isForwardDeclaration() {
    TypeNameDeclaration declaration = getTypeNameDeclaration();
    return declaration != null && declaration.isForwardDeclaration();
  }

  @Override
  @NotNull
  public Type getType() {
    return getTypeNode().getType();
  }
}
