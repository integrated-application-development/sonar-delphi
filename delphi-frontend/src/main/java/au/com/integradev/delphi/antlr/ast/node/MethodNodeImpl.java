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

import au.com.integradev.delphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import au.com.integradev.delphi.symbol.declaration.MethodDirective;
import au.com.integradev.delphi.symbol.declaration.MethodKind;
import au.com.integradev.delphi.symbol.declaration.MethodNameDeclaration;
import au.com.integradev.delphi.type.DelphiType;
import au.com.integradev.delphi.type.Type;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;

public abstract class MethodNodeImpl extends AbstractDelphiNode implements MethodNode {
  protected MethodNodeImpl(Token token) {
    super(token);
  }

  protected MethodNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public String getImage() {
    return getMethodHeading().getImage();
  }

  @Override
  public MethodHeadingNode getMethodHeading() {
    return (MethodHeadingNode) jjtGetChild(0);
  }

  @Override
  public MethodNameNode getMethodNameNode() {
    return getMethodHeading().getMethodNameNode();
  }

  @Override
  public String simpleName() {
    return getMethodHeading().simpleName();
  }

  @Override
  public String fullyQualifiedName() {
    return getMethodHeading().fullyQualifiedName();
  }

  @Override
  public List<FormalParameterData> getParameters() {
    return getMethodHeading().getParameters();
  }

  @Override
  public List<Type> getParameterTypes() {
    return getMethodHeading().getParameterTypes();
  }

  @Override
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

  @Override
  public MethodKind getMethodKind() {
    return getMethodHeading().getMethodKind();
  }

  @Override
  public Set<MethodDirective> getDirectives() {
    return getMethodHeading().getDirectives();
  }

  @Override
  public boolean hasDirective(MethodDirective directive) {
    return getDirectives().contains(directive);
  }

  @Override
  public boolean isConstructor() {
    return getMethodKind() == MethodKind.CONSTRUCTOR;
  }

  @Override
  public boolean isDestructor() {
    return getMethodKind() == MethodKind.DESTRUCTOR;
  }

  @Override
  public boolean isFunction() {
    return getMethodKind() == MethodKind.FUNCTION;
  }

  @Override
  public boolean isOperator() {
    return getMethodKind() == MethodKind.OPERATOR;
  }

  @Override
  public boolean isProcedure() {
    return getMethodKind() == MethodKind.PROCEDURE;
  }

  @Override
  public boolean isClassMethod() {
    return getMethodHeading().isClassMethod();
  }

  @Override
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

  @Override
  @Nullable
  public MethodNameDeclaration getMethodNameDeclaration() {
    return getMethodHeading().getMethodNameNode().getMethodNameDeclaration();
  }

  protected abstract VisibilityType createVisibility();
}
