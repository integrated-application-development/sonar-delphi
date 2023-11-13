/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
import au.com.integradev.delphi.symbol.QualifiedNameImpl;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.apache.commons.lang3.StringUtils;
import org.sonar.plugins.communitydelphi.api.ast.AttributeListNode;
import org.sonar.plugins.communitydelphi.api.ast.ClassHelperTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ClassReferenceTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ClassTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.EnumTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ObjectTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.PointerTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.RecordHelperTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.RecordTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.SimpleNameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.StrongAliasTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;
import org.sonar.plugins.communitydelphi.api.ast.WeakAliasTypeNode;
import org.sonar.plugins.communitydelphi.api.symbol.QualifiedName;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;

public final class TypeDeclarationNodeImpl extends DelphiNodeImpl implements TypeDeclarationNode {
  private Boolean isNestedType;
  private QualifiedName qualifiedName;
  private String qualifiedNameExcludingUnit;

  public TypeDeclarationNodeImpl(Token token) {
    super(token);
  }

  public TypeDeclarationNodeImpl(int tokenType) {
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

  @Override
  public SimpleNameDeclarationNode getTypeNameNode() {
    return (SimpleNameDeclarationNode) getChild(0);
  }

  @Override
  public TypeNode getTypeNode() {
    return (TypeNode) getChild(1);
  }

  @Override
  public AttributeListNode getAttributeList() {
    return getFirstChildOfType(AttributeListNode.class);
  }

  @Override
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

  @Override
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
    names.addFirst(getUnitName());

    qualifiedName = new QualifiedNameImpl(names);
  }

  @Override
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

  @Override
  public boolean isClass() {
    return getTypeNode() instanceof ClassTypeNode;
  }

  @Override
  public boolean isClassHelper() {
    return getTypeNode() instanceof ClassHelperTypeNode;
  }

  @Override
  public boolean isClassReference() {
    return getTypeNode() instanceof ClassReferenceTypeNode;
  }

  @Override
  public boolean isEnum() {
    return getTypeNode() instanceof EnumTypeNode;
  }

  @Override
  public boolean isInterface() {
    return getTypeNode() instanceof InterfaceTypeNode;
  }

  @Override
  public boolean isObject() {
    return getTypeNode() instanceof ObjectTypeNode;
  }

  @Override
  public boolean isRecord() {
    return getTypeNode() instanceof RecordTypeNode;
  }

  @Override
  public boolean isRecordHelper() {
    return getTypeNode() instanceof RecordHelperTypeNode;
  }

  @Override
  public boolean isPointer() {
    return getTypeNode() instanceof PointerTypeNode;
  }

  @Override
  public boolean isNestedType() {
    if (isNestedType == null) {
      isNestedType = getFirstParentOfType(TypeDeclarationNode.class) != null;
    }
    return isNestedType;
  }

  @Override
  public boolean isWeakAlias() {
    return getTypeNode() instanceof WeakAliasTypeNode;
  }

  @Override
  public boolean isStrongAlias() {
    return getTypeNode() instanceof StrongAliasTypeNode;
  }

  @Override
  public boolean isForwardDeclaration() {
    TypeNameDeclaration declaration = getTypeNameDeclaration();
    return declaration != null && declaration.isForwardDeclaration();
  }

  @Override
  public Type getType() {
    return getTypeNode().getType();
  }
}
