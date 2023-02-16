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
import au.com.integradev.delphi.symbol.QualifiedNameImpl;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.QualifiedName;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.GenericArgumentsNode;
import org.sonar.plugins.communitydelphi.api.ast.GenericDefinitionNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodNameNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.SimpleNameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;

public final class MethodNameNodeImpl extends DelphiNodeImpl implements MethodNameNode {
  private QualifiedName qualifiedName;
  private MethodNameDeclaration methodNameDeclaration;
  private List<NameOccurrence> usages;

  public MethodNameNodeImpl(Token token) {
    super(token);
  }

  public MethodNameNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public NameReferenceNode getNameReferenceNode() {
    return getFirstChildOfType(NameReferenceNode.class);
  }

  @Override
  public SimpleNameDeclarationNode getNameDeclarationNode() {
    return getFirstChildOfType(SimpleNameDeclarationNode.class);
  }

  @Override
  public QualifiedName getQualifiedName() {
    if (qualifiedName == null) {
      NameReferenceNode nameReference = getNameReferenceNode();
      if (nameReference != null) {
        qualifiedName = nameReference.getQualifiedName();
      } else {
        qualifiedName = QualifiedNameImpl.of(getNameDeclarationNode().getImage());
      }
    }
    return qualifiedName;
  }

  @Override
  @Nullable
  public MethodNameDeclaration getMethodNameDeclaration() {
    if (methodNameDeclaration == null) {
      methodNameDeclaration = findMethodNameDeclaration();
    }

    return methodNameDeclaration;
  }

  @Override
  public String simpleNameWithTypeParameters() {
    NameDeclarationNode declarationNode = getNameDeclarationNode();
    NameReferenceNode referenceNode = getNameReferenceNode();
    String typeParameters = "";

    if (declarationNode != null) {
      GenericDefinitionNode genericDefinition = declarationNode.getGenericDefinition();
      if (genericDefinition != null) {
        typeParameters = genericDefinition.getImage();
      }
    } else if (referenceNode != null) {
      GenericArgumentsNode genericArguments = referenceNode.getLastName().getGenericArguments();
      if (genericArguments != null) {
        typeParameters = genericArguments.getImage();
      }
    }

    return simpleName() + typeParameters;
  }

  @Override
  public String getImage() {
    return simpleName();
  }

  @Override
  public void setMethodNameDeclaration(MethodNameDeclaration declaration) {
    this.methodNameDeclaration = declaration;
  }

  @Override
  public List<NameOccurrence> getUsages() {
    if (usages == null) {
      if (methodNameDeclaration != null) {
        usages = methodNameDeclaration.getScope().getOccurrencesFor(methodNameDeclaration);
      } else {
        usages = Collections.emptyList();
      }
    }
    return usages;
  }

  private MethodNameDeclaration findMethodNameDeclaration() {
    // Interface method declaration
    NameDeclarationNode declarationNode = getNameDeclarationNode();
    if (declarationNode != null) {
      NameDeclaration declaration = declarationNode.getNameDeclaration();
      if (declaration instanceof MethodNameDeclaration) {
        return (MethodNameDeclaration) declaration;
      }
    }

    // Implementation method referring back to interface declaration
    NameReferenceNode referenceNode = getNameReferenceNode();
    if (referenceNode != null) {
      for (NameReferenceNode name : referenceNode.flatten()) {
        NameDeclaration declaration = name.getNameDeclaration();
        if (declaration instanceof MethodNameDeclaration) {
          return (MethodNameDeclaration) declaration;
        }
      }
    }

    return null;
  }
}
