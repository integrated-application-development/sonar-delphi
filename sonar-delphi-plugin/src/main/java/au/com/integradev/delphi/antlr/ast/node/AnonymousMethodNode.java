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
import au.com.integradev.delphi.symbol.declaration.MethodKind;
import au.com.integradev.delphi.type.DelphiType;
import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.parameter.FormalParameter;
import java.util.Collections;
import java.util.stream.Collectors;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;

public final class AnonymousMethodNode extends ExpressionNode {
  private String image;
  private MethodKind methodKind;

  public AnonymousMethodNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public MethodParametersNode getMethodParametersNode() {
    return getFirstChildOfType(MethodParametersNode.class);
  }

  public MethodReturnTypeNode getReturnTypeNode() {
    return getFirstChildOfType(MethodReturnTypeNode.class);
  }

  public Type getReturnType() {
    return getReturnTypeNode().getTypeNode().getType();
  }

  private MethodKind getMethodKind() {
    if (methodKind == null) {
      methodKind = MethodKind.fromTokenType(jjtGetId());
    }
    return methodKind;
  }

  public boolean isFunction() {
    return getMethodKind() == MethodKind.FUNCTION;
  }

  private String getParameterSignature() {
    MethodParametersNode parameters = getMethodParametersNode();
    return parameters != null ? getMethodParametersNode().getImage() : "";
  }

  private String getReturnTypeSignature() {
    MethodReturnTypeNode returnType = getReturnTypeNode();
    return returnType != null ? (" : " + getReturnTypeNode().getTypeNode().getImage()) : "";
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = getToken().getImage() + getParameterSignature() + getReturnTypeSignature();
    }
    return image;
  }

  @Override
  @NotNull
  public Type createType() {
    MethodParametersNode parameters = getMethodParametersNode();
    MethodReturnTypeNode returnTypeNode = getReturnTypeNode();

    return getTypeFactory()
        .anonymous(
            parameters == null
                ? Collections.emptyList()
                : parameters.getParameters().stream()
                    .map(FormalParameter::create)
                    .collect(Collectors.toUnmodifiableList()),
            returnTypeNode == null
                ? DelphiType.voidType()
                : returnTypeNode.getTypeNode().getType());
  }
}
