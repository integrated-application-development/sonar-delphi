package org.sonar.plugins.delphi.symbol.resolve;

import java.math.BigInteger;
import java.util.Objects;
import net.sourceforge.pmd.lang.ast.Node;
import org.sonar.plugins.delphi.antlr.ast.node.ArrayConstructorNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.LiteralNode;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.CollectionType;
import org.sonar.plugins.delphi.type.Type.IntegerType;

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
