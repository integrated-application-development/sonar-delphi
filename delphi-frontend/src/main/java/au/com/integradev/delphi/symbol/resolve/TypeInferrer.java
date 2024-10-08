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

import au.com.integradev.delphi.type.factory.ArrayOption;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import java.util.Set;
import org.sonar.plugins.communitydelphi.api.ast.ArrayConstructorNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.IntegerLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.utils.ExpressionNodeUtils;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.IntegerType;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;
import org.sonar.plugins.communitydelphi.api.type.Typed;

public final class TypeInferrer {
  private final TypeFactory typeFactory;

  public TypeInferrer(TypeFactory typeFactory) {
    this.typeFactory = typeFactory;
  }

  public Type infer(Typed typed) {
    Type type = typed == null ? TypeFactory.unknownType() : typed.getType();

    if (typed instanceof ExpressionNode) {
      ExpressionNode expression = (ExpressionNode) typed;
      Node arrayConstructor = expression.skipParentheses().getChild(0);

      if (arrayConstructor instanceof ArrayConstructorNode) {
        type = inferArrayConstructor((ArrayConstructorNode) arrayConstructor);
      } else {
        type = widenIntegerLiteralToInteger(type, expression);
      }
    }

    if (type.isProcedural()) {
      Type returnType = ((ProceduralType) type).returnType();
      if (!returnType.isVoid()) {
        type = returnType;
      }
    }

    type = widenFloatingPointToExtended(type);

    return type;
  }

  private Type inferArrayConstructor(ArrayConstructorNode arrayConstructor) {
    Type element =
        arrayConstructor.getElements().stream()
            .map(this::infer)
            .max(TypeInferrer::compareTypeSize)
            .orElse(TypeFactory.voidType());

    return ((TypeFactoryImpl) typeFactory).array(null, element, Set.of(ArrayOption.DYNAMIC));
  }

  private Type widenIntegerLiteralToInteger(Type type, ExpressionNode expression) {
    IntegerLiteralNode literal = ExpressionNodeUtils.unwrapInteger(expression);
    if (literal != null) {
      IntegerType integer = (IntegerType) typeFactory.getIntrinsic(IntrinsicType.INTEGER);
      if (integer.min().compareTo(literal.getValue()) <= 0
          && integer.max().compareTo(literal.getValue()) >= 0) {
        type = typeFactory.getIntrinsic(IntrinsicType.INTEGER);
      }
    }
    return type;
  }

  private Type widenFloatingPointToExtended(Type type) {
    if (type.isReal() && !type.is(IntrinsicType.COMP) && !type.is(IntrinsicType.CURRENCY)) {
      // Inline vars will widen any floating point real type to Extended, even when the assigned
      // expression's type is explicitly known.
      // See: https://quality.embarcadero.com/browse/RSP-42107
      type = typeFactory.getIntrinsic(IntrinsicType.EXTENDED);
    }
    return type;
  }

  private static int compareTypeSize(Type a, Type b) {
    if (a.size() > b.size()) {
      return 1;
    } else if (a.size() < b.size()) {
      return -1;
    } else if (a instanceof IntegerType && b instanceof IntegerType) {
      return ((IntegerType) a).max().compareTo(((IntegerType) b).max());
    } else {
      return 0;
    }
  }
}
