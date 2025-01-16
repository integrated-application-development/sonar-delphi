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
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.type.parameter.FormalParameter;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.AnonymousMethodHeadingNode;
import org.sonar.plugins.communitydelphi.api.ast.AnonymousMethodNode;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineParametersNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineReturnTypeNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineKind;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType.ProceduralKind;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public final class AnonymousMethodNodeImpl extends ExpressionNodeImpl
    implements AnonymousMethodNode {
  private String image;

  public AnonymousMethodNodeImpl(Token token) {
    super(token);
  }

  public AnonymousMethodNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public AnonymousMethodHeadingNode getAnonymousMethodHeading() {
    return (AnonymousMethodHeadingNode) getChild(0);
  }

  @Override
  public RoutineParametersNode getRoutineParametersNode() {
    return getAnonymousMethodHeading().getRoutineParametersNode();
  }

  @Override
  public RoutineReturnTypeNode getReturnTypeNode() {
    return getAnonymousMethodHeading().getReturnTypeNode();
  }

  @Override
  public Type getReturnType() {
    return getReturnTypeNode().getTypeNode().getType();
  }

  @Override
  public RoutineKind getRoutineKind() {
    return getAnonymousMethodHeading().getRoutineKind();
  }

  @Override
  public Set<RoutineDirective> getDirectives() {
    return getAnonymousMethodHeading().getDirectives();
  }

  @Override
  public boolean hasDirective(RoutineDirective directive) {
    return getDirectives().contains(directive);
  }

  @Override
  public boolean isFunction() {
    return getRoutineKind() == RoutineKind.FUNCTION;
  }

  @Override
  public boolean isProcedure() {
    return getRoutineKind() == RoutineKind.PROCEDURE;
  }

  @Override
  public CompoundStatementNode getStatementBlock() {
    return (CompoundStatementNode) getChild(1);
  }

  @Override
  public boolean isEmpty() {
    return getStatementBlock().isEmpty();
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = getToken().getImage() + getParameterSignature() + getReturnTypeSignature();
    }
    return image;
  }

  private String getParameterSignature() {
    RoutineParametersNode parameters = getRoutineParametersNode();
    return parameters != null ? getRoutineParametersNode().getImage() : "";
  }

  private String getReturnTypeSignature() {
    RoutineReturnTypeNode returnType = getReturnTypeNode();
    return returnType != null ? (" : " + getReturnTypeNode().getTypeNode().getImage()) : "";
  }

  @Override
  @Nonnull
  protected Type createType() {
    RoutineParametersNode parameters = getRoutineParametersNode();
    RoutineReturnTypeNode returnTypeNode = getReturnTypeNode();

    return ((TypeFactoryImpl) getTypeFactory())
        .createProcedural(
            ProceduralKind.ANONYMOUS,
            parameters == null
                ? Collections.emptyList()
                : parameters.getParameters().stream()
                    .map(FormalParameter::create)
                    .collect(Collectors.toUnmodifiableList()),
            returnTypeNode == null
                ? TypeFactory.voidType()
                : returnTypeNode.getTypeNode().getType(),
            getDirectives());
  }
}
