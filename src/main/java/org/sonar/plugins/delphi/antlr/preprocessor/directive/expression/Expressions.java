package org.sonar.plugins.delphi.antlr.preprocessor.directive.expression;

import static org.sonar.plugins.delphi.antlr.preprocessor.directive.CompilerDirective.Expression.ConstExpressionType.DECIMAL;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.CompilerDirective.Expression.ConstExpressionType.INTEGER;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.add;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.and;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.createBoolean;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.createDecimal;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.createInteger;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.createSet;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.createString;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.div;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.divide;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.greaterThan;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.greaterThanEqual;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.in;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.isEqual;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.lessThan;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.lessThanEqual;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.mod;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.multiply;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.notEqual;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.or;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.shl;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.shr;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.subtract;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.unknownValue;
import static org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.ExpressionValues.xor;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.plugins.delphi.antlr.preprocessor.DelphiPreprocessor;
import org.sonar.plugins.delphi.antlr.preprocessor.directive.CompilerDirective.Expression;
import org.sonar.plugins.delphi.antlr.preprocessor.directive.expression.Token.TokenType;

public class Expressions {
  private Expressions() {
    // Utility class
  }

  public static Expression binary(Expression left, TokenType operator, Expression right) {
    return new BinaryExpression(left, operator, right);
  }

  public static Expression unary(TokenType operator, Expression expression) {
    return new UnaryExpression(operator, expression);
  }

  public static Expression literal(TokenType type, String value) {
    return new LiteralExpression(type, value);
  }

  public static Expression set(Set<Expression> elements) {
    return new SetExpression(elements);
  }

  public static Expression emptySet() {
    return new SetExpression(Collections.emptySet());
  }

  public static Expression nameReference(String name) {
    return new NameReferenceExpression(name);
  }

  public static Expression invocation(String name, List<Expression> arguments) {
    return new InvocationExpression(name, arguments);
  }

  static class BinaryExpression implements Expression {
    private final Expression leftExpression;
    private final TokenType operator;
    private final Expression rightExpression;

    BinaryExpression(Expression leftExpression, TokenType operator, Expression rightExpression) {
      this.leftExpression = leftExpression;
      this.operator = operator;
      this.rightExpression = rightExpression;
    }

    @Override
    public ExpressionValue evaluate(DelphiPreprocessor preprocessor) {
      ExpressionValue left = leftExpression.evaluate(preprocessor);
      ExpressionValue right = rightExpression.evaluate(preprocessor);

      switch (operator) {
        case PLUS:
          return add(left, right);
        case MINUS:
          return subtract(left, right);
        case MULTIPLY:
          return multiply(left, right);
        case DIVIDE:
          return divide(left, right);
        case DIV:
          return div(left, right);
        case MOD:
          return mod(left, right);
        case SHL:
          return shl(left, right);
        case SHR:
          return shr(left, right);
        case EQUALS:
          return isEqual(left, right);
        case GREATER_THAN:
          return greaterThan(left, right);
        case LESS_THAN:
          return lessThan(left, right);
        case GREATER_THAN_EQUAL:
          return greaterThanEqual(left, right);
        case LESS_THAN_EQUAL:
          return lessThanEqual(left, right);
        case NOT_EQUALS:
          return notEqual(left, right);
        case IN:
          return in(left, right);
        case AND:
          return and(left, right);
        case OR:
          return or(left, right);
        case XOR:
          return xor(left, right);
        default:
          throw new AssertionError("Unhandled binary expression operator: " + operator.name());
      }
    }
  }

  static class UnaryExpression implements Expression {
    private final TokenType operator;
    private final Expression expression;

    private UnaryExpression(TokenType operator, Expression expression) {
      this.operator = operator;
      this.expression = expression;
    }

    @Override
    public ExpressionValue evaluate(DelphiPreprocessor preprocessor) {
      ExpressionValue value = expression.evaluate(preprocessor);

      switch (operator) {
        case PLUS:
          if (value.type() == INTEGER || value.type() == DECIMAL) {
            return value;
          } else {
            return unknownValue();
          }

        case MINUS:
          return ExpressionValues.negate(value);

        case NOT:
          return ExpressionValues.not(value);

        default:
          throw new AssertionError("Unhandled unary expression operator: " + operator.name());
      }
    }
  }

  static class LiteralExpression implements Expression {
    private final ExpressionValue value;

    private LiteralExpression(TokenType type, String text) {
      value = createValue(type, text);
    }

    private static ExpressionValue createValue(TokenType type, String text) {
      switch (type) {
        case INTEGER:
          return createInteger(Integer.parseInt(text));
        case DECIMAL:
          return createDecimal(Double.parseDouble(text));
        case STRING:
          return createString(text);
        default:
          throw new AssertionError("Unhandled literal expression type: " + type.name());
      }
    }

    @Override
    public ExpressionValue evaluate(DelphiPreprocessor preprocessor) {
      return value;
    }
  }

  static class SetExpression implements Expression {
    private final Set<Expression> elements;

    private SetExpression(Set<Expression> elements) {
      this.elements = elements;
    }

    @Override
    public ExpressionValue evaluate(DelphiPreprocessor preprocessor) {
      Set<ExpressionValue> elementValues =
          elements.stream()
              .map(expression -> expression.evaluate(preprocessor))
              .collect(Collectors.toSet());

      return createSet(elementValues);
    }
  }

  static class NameReferenceExpression implements Expression {
    private final String name;

    private NameReferenceExpression(String name) {
      this.name = name;
    }

    @Override
    public ExpressionValue evaluate(DelphiPreprocessor preprocessor) {
      if (this.name.equalsIgnoreCase("True")) {
        return createBoolean(true);
      } else if (this.name.equalsIgnoreCase("False")) {
        return createBoolean(false);
      }
      return unknownValue();
    }
  }

  static class InvocationExpression implements Expression {
    private final String name;
    private final List<Expression> arguments;

    private InvocationExpression(String name, List<Expression> arguments) {
      this.name = name;
      this.arguments = arguments;
    }

    @Override
    public ExpressionValue evaluate(DelphiPreprocessor preprocessor) {
      if (name.equalsIgnoreCase("Defined") && !arguments.isEmpty()) {
        Expression argument = arguments.get(0);
        if (argument instanceof NameReferenceExpression) {
          boolean isDefined = preprocessor.isDefined(((NameReferenceExpression) argument).name);
          return createBoolean(isDefined);
        }
      }
      return unknownValue();
    }
  }
}
