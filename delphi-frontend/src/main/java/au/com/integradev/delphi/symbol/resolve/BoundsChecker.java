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

import org.sonar.plugins.communitydelphi.api.ast.ArrayConstructorNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.LiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Type.CollectionType;
import au.com.integradev.delphi.type.Type.IntegerType;
import java.math.BigInteger;
import java.util.Objects;

interface BoundsChecker {
  static BoundsChecker forType(Type type) {
    if (type.isInteger()) {
      return new IntegerBoundsChecker((IntegerType) type);
    } else if (type instanceof CollectionType) {
      return new CollectionBoundsChecker((CollectionType) type);
    } else {
      return new DefaultBoundsChecker();
    }
  }

  boolean violatesBounds(ExpressionNode expression);

  class DefaultBoundsChecker implements BoundsChecker {
    @Override
    public boolean violatesBounds(ExpressionNode expression) {
      return false;
    }
  }

  class IntegerBoundsChecker implements BoundsChecker {
    private final IntegerType type;

    private IntegerBoundsChecker(IntegerType type) {
      this.type = type;
    }

    @Override
    public boolean violatesBounds(ExpressionNode expression) {
      if (expression.isIntegerLiteral()) {
        LiteralNode literal = Objects.requireNonNull(expression.extractLiteral());
        BigInteger value = literal.getValueAsBigInteger();
        return type.min().compareTo(value) > 0 || type.max().compareTo(value) < 0;
      }
      return false;
    }
  }

  class CollectionBoundsChecker implements BoundsChecker {
    private final CollectionType type;

    private CollectionBoundsChecker(CollectionType type) {
      this.type = type;
    }

    @Override
    public boolean violatesBounds(ExpressionNode expression) {
      if (expression.getType().isArrayConstructor()) {
        Node arrayConstructor = expression.skipParentheses().jjtGetChild(0);
        return arrayConstructor instanceof ArrayConstructorNode
            && ((ArrayConstructorNode) arrayConstructor)
                .getElements().stream().anyMatch(this::elementViolatesBounds);
      }
      return false;
    }

    protected boolean elementViolatesBounds(ExpressionNode element) {
      return BoundsChecker.forType(type.elementType()).violatesBounds(element);
    }
  }
}
