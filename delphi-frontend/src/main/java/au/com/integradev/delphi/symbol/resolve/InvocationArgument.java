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
package au.com.integradev.delphi.symbol.resolve;

import au.com.integradev.delphi.antlr.ast.node.DelphiNodeImpl;
import com.google.common.base.Preconditions;
import java.math.BigInteger;
import java.util.Objects;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.IntegerLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.utils.ExpressionNodeUtils;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;
import org.sonar.plugins.communitydelphi.api.type.Typed;

public class InvocationArgument implements Typed {
  private final ExpressionNode expression;
  private Type type;
  @Nullable private NameResolver resolver;

  InvocationArgument(ExpressionNode expression) {
    this.expression = expression;
    this.type = null;

    if (expression instanceof PrimaryExpressionNode) {
      PrimaryExpressionNode primary = (PrimaryExpressionNode) expression;
      this.resolver = new NameResolver(((DelphiNodeImpl) primary).getTypeFactory());
      resolver.readPrimaryExpression(primary);
      this.type = resolver.getApproximateType();
    }

    if (this.type == null) {
      this.type = expression.getType();
    }
  }

  void resolve(Type parameterType) {
    if (resolver != null) {
      if (isRoutineReference(parameterType)) {
        resolver.disambiguateRoutineReference((ProceduralType) parameterType);
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

  boolean isRoutineReference(Type parameterType) {
    return looksLikeProceduralReference()
        && Objects.requireNonNull(resolver).getApproximateType().isRoutine()
        && parameterType.isProcedural();
  }

  boolean violatesBounds(Type type) {
    return BoundsChecker.forType(type).violatesBounds(expression);
  }

  boolean isImplicitlyConvertibleToNilPointer() {
    IntegerLiteralNode literal = ExpressionNodeUtils.unwrapInteger(expression);
    return literal != null
        && (literal.getRadix() == 10 || literal.getRadix() == 16)
        && literal.getValue().equals(BigInteger.ZERO);
  }

  Type findRoutineReferenceType(Type parameterType) {
    Preconditions.checkArgument(parameterType instanceof ProceduralType);
    Preconditions.checkNotNull(resolver);

    NameResolver clone = new NameResolver(resolver);
    clone.disambiguateRoutineReference((ProceduralType) parameterType);
    if (!clone.isAmbiguous()) {
      return clone.getApproximateType();
    }

    return TypeFactory.unknownType();
  }

  @Override
  public Type getType() {
    return type;
  }
}
