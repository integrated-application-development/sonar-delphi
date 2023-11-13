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

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.api.ast.RoutineHeadingNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineNameNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineReturnTypeNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineKind;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public abstract class RoutineNodeImpl extends DelphiNodeImpl implements RoutineNode {
  protected RoutineNodeImpl(Token token) {
    super(token);
  }

  protected RoutineNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public String getImage() {
    return getRoutineHeading().getImage();
  }

  @Override
  public RoutineHeadingNode getRoutineHeading() {
    return (RoutineHeadingNode) getChild(0);
  }

  @Override
  public RoutineNameNode getRoutineNameNode() {
    return getRoutineHeading().getRoutineNameNode();
  }

  @Override
  public String simpleName() {
    return getRoutineHeading().simpleName();
  }

  @Override
  public String fullyQualifiedName() {
    return getRoutineHeading().fullyQualifiedName();
  }

  @Override
  public List<FormalParameterData> getParameters() {
    return getRoutineHeading().getParameters();
  }

  @Override
  public List<Type> getParameterTypes() {
    return getRoutineHeading().getParameterTypes();
  }

  @Override
  public Type getReturnType() {
    if (isProcedure() || isConstructor()) {
      return TypeFactory.voidType();
    }

    RoutineReturnTypeNode returnTypeNode = getRoutineHeading().getRoutineReturnType();
    if (returnTypeNode != null) {
      return returnTypeNode.getTypeNode().getType();
    }

    return TypeFactory.unknownType();
  }

  @Override
  public RoutineKind getRoutineKind() {
    return getRoutineHeading().getRoutineKind();
  }

  @Override
  public Set<RoutineDirective> getDirectives() {
    return getRoutineHeading().getDirectives();
  }

  @Override
  public boolean hasDirective(RoutineDirective directive) {
    return getDirectives().contains(directive);
  }

  @Override
  public boolean isConstructor() {
    return getRoutineKind() == RoutineKind.CONSTRUCTOR;
  }

  @Override
  public boolean isDestructor() {
    return getRoutineKind() == RoutineKind.DESTRUCTOR;
  }

  @Override
  public boolean isFunction() {
    return getRoutineKind() == RoutineKind.FUNCTION;
  }

  @Override
  public boolean isOperator() {
    return getRoutineKind() == RoutineKind.OPERATOR;
  }

  @Override
  public boolean isProcedure() {
    return getRoutineKind() == RoutineKind.PROCEDURE;
  }

  @Override
  public boolean isClassMethod() {
    return getRoutineHeading().isClassMethod();
  }

  @Override
  public String getTypeName() {
    return getRoutineHeading().getTypeName();
  }

  @Override
  public final VisibilityType getVisibility() {
    RoutineNameDeclaration declaration = getRoutineNameDeclaration();
    if (declaration != null) {
      return declaration.getVisibility();
    } else {
      return createVisibility();
    }
  }

  @Override
  @Nullable
  public RoutineNameDeclaration getRoutineNameDeclaration() {
    return getRoutineHeading().getRoutineNameNode().getRoutineNameDeclaration();
  }

  protected abstract VisibilityType createVisibility();
}
