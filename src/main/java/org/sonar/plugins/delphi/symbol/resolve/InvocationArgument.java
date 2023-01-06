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
package org.sonar.plugins.delphi.symbol.resolve;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.LiteralNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Typed;

public class InvocationArgument implements Typed {
  private final ExpressionNode expression;
  private Type type;
  @Nullable private NameResolver resolver;

  InvocationArgument(ExpressionNode expression) {
    this.expression = expression;
    this.type = null;

    if (expression instanceof PrimaryExpressionNode) {
      PrimaryExpressionNode primary = (PrimaryExpressionNode) expression;
      this.resolver = new NameResolver(primary.getTypeFactory());
      resolver.readPrimaryExpression(primary);
      this.type = resolver.getApproximateType();
    }

    if (this.type == null) {
      this.type = expression.getType();
    }
  }

  void resolve(Type parameterType) {
    if (resolver != null) {
      if (isMethodReference(parameterType)) {
        disambiguateMethodReference(resolver, parameterType);
      } else if (!resolver.isExplicitInvocation()) {
        resolver.disambiguateImplicitEmptyArgumentList();
      }
      resolver.addToSymbolTable();
    }
  }

  boolean looksLikeProceduralReference() {
    return expression instanceof PrimaryExpressionNode
        && resolver != null
        && !resolver.isExplicitInvocation()
        && resolver.getApproximateType().isProcedural();
  }

  boolean isMethodReference(Type parameterType) {
    return looksLikeProceduralReference()
        && Objects.requireNonNull(resolver).getApproximateType().isMethod()
        && parameterType.isProcedural();
  }

  boolean violatesBounds(Type type) {
    return BoundsChecker.forType(type).violatesBounds(expression);
  }

  boolean isImplicitlyConvertibleToNilPointer() {
    LiteralNode literal = expression.extractLiteral();
    return literal != null
        && (literal.isIntegerLiteral() || literal.isHexadecimalLiteral())
        && literal.getValueAsInt() == 0;
  }

  Type findMethodReferenceType(Type parameterType) {
    checkArgument(parameterType instanceof ProceduralType);
    checkNotNull(resolver);

    NameResolver clone = new NameResolver(resolver);
    disambiguateMethodReference(clone, parameterType);
    return clone.getApproximateType();
  }

  private static void disambiguateMethodReference(NameResolver resolver, Type parameterType) {
    resolver.disambiguateMethodReference((ProceduralType) parameterType);
    resolver.checkAmbiguity();
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
  }
}
