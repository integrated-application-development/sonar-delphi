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
package au.com.integradev.delphi.symbol.resolve;

import au.com.integradev.delphi.type.ArrayOption;
import au.com.integradev.delphi.type.DelphiType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.Typed;
import au.com.integradev.delphi.type.factory.TypeFactory;
import au.com.integradev.delphi.type.intrinsic.IntrinsicType;
import java.util.Comparator;
import java.util.Set;
import org.sonar.plugins.communitydelphi.api.ast.ArrayConstructorNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;

public final class TypeInferrer {
  private final TypeFactory typeFactory;

  public TypeInferrer(TypeFactory typeFactory) {
    this.typeFactory = typeFactory;
  }

  public Type infer(Typed typed) {
    Type type = typed == null ? DelphiType.unknownType() : typed.getType();

    if (typed instanceof ExpressionNode) {
      ExpressionNode expression = (ExpressionNode) typed;
      Node arrayConstructor = expression.skipParentheses().jjtGetChild(0);

      if (arrayConstructor instanceof ArrayConstructorNode) {
        type = inferArrayConstructor((ArrayConstructorNode) arrayConstructor);
      } else if (expression.isIntegerLiteral() && type.size() <= 4) {
        type = typeFactory.getIntrinsic(IntrinsicType.INTEGER);
      }
    }

    if (type.isProcedural()) {
      Type returnType = ((ProceduralType) type).returnType();
      if (!returnType.isVoid()) {
        type = returnType;
      }
    }

    return type;
  }

  private Type inferArrayConstructor(ArrayConstructorNode arrayConstructor) {
    Type element =
        arrayConstructor.getElements().stream()
            .map(this::infer)
            .max(Comparator.comparingInt(Type::size))
            .orElse(DelphiType.voidType());

    return typeFactory.array(null, element, Set.of(ArrayOption.DYNAMIC));
  }
}
