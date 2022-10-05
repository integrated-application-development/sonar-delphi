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
package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.delphi.symbol.declaration.MethodDirective;
import org.sonar.plugins.delphi.symbol.declaration.MethodKind;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;

public abstract class MethodNode extends DelphiNode implements Visibility {
  protected MethodNode(Token token) {
    super(token);
  }

  protected MethodNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public String getImage() {
    return getMethodHeading().getImage();
  }

  public MethodHeadingNode getMethodHeading() {
    return (MethodHeadingNode) jjtGetChild(0);
  }

  public MethodNameNode getMethodNameNode() {
    return getMethodHeading().getMethodNameNode();
  }

  public String simpleName() {
    return getMethodHeading().simpleName();
  }

  public String fullyQualifiedName() {
    return getMethodHeading().fullyQualifiedName();
  }

  public List<FormalParameterData> getParameters() {
    return getMethodHeading().getParameters();
  }

  public List<Type> getParameterTypes() {
    return getMethodHeading().getParameterTypes();
  }

  public Type getReturnType() {
    if (isProcedure() || isConstructor()) {
      return DelphiType.voidType();
    }

    MethodReturnTypeNode returnTypeNode = getMethodHeading().getMethodReturnType();
    if (returnTypeNode != null) {
      return returnTypeNode.getTypeNode().getType();
    }

    return DelphiType.unknownType();
  }

  public MethodKind getMethodKind() {
    return getMethodHeading().getMethodKind();
  }

  public Set<MethodDirective> getDirectives() {
    return getMethodHeading().getDirectives();
  }

  public boolean hasDirective(MethodDirective directive) {
    return getDirectives().contains(directive);
  }

  public boolean isConstructor() {
    return getMethodKind() == MethodKind.CONSTRUCTOR;
  }

  public boolean isDestructor() {
    return getMethodKind() == MethodKind.DESTRUCTOR;
  }

  public boolean isFunction() {
    return getMethodKind() == MethodKind.FUNCTION;
  }

  public boolean isOperator() {
    return getMethodKind() == MethodKind.OPERATOR;
  }

  public boolean isProcedure() {
    return getMethodKind() == MethodKind.PROCEDURE;
  }

  public boolean isClassMethod() {
    return getMethodHeading().isClassMethod();
  }

  public String getTypeName() {
    return getMethodHeading().getTypeName();
  }

  @Override
  public final VisibilityType getVisibility() {
    MethodNameDeclaration declaration = getMethodNameDeclaration();
    if (declaration != null) {
      return declaration.getVisibility();
    } else {
      return createVisibility();
    }
  }

  @Nullable
  public MethodNameDeclaration getMethodNameDeclaration() {
    return getMethodHeading().getMethodNameNode().getMethodNameDeclaration();
  }

  @Nullable
  public abstract TypeNameDeclaration getTypeDeclaration();

  protected abstract VisibilityType createVisibility();
}
