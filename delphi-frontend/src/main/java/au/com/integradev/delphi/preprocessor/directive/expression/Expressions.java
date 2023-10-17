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
package au.com.integradev.delphi.preprocessor.directive.expression;

import static au.com.integradev.delphi.preprocessor.directive.expression.ExpressionValues.createBoolean;
import static au.com.integradev.delphi.preprocessor.directive.expression.ExpressionValues.createDecimal;
import static au.com.integradev.delphi.preprocessor.directive.expression.ExpressionValues.createInteger;
import static au.com.integradev.delphi.preprocessor.directive.expression.ExpressionValues.createSet;
import static au.com.integradev.delphi.preprocessor.directive.expression.ExpressionValues.createString;
import static au.com.integradev.delphi.preprocessor.directive.expression.ExpressionValues.unknownValue;

import au.com.integradev.delphi.preprocessor.DelphiPreprocessor;
import au.com.integradev.delphi.preprocessor.directive.expression.Expression.ExpressionValue.BinaryEvaluator;
import au.com.integradev.delphi.preprocessor.directive.expression.Expression.ExpressionValue.UnaryEvaluator;
import au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType;
import com.google.common.base.Preconditions;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public final class Expressions {
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
    private static final Map<TokenType, BinaryEvaluator> EVALUATORS;

    static {
      EVALUATORS = new EnumMap<>(TokenType.class);
      EVALUATORS.put(TokenType.PLUS, ExpressionValues::add);
      EVALUATORS.put(TokenType.MINUS, ExpressionValues::subtract);
      EVALUATORS.put(TokenType.MULTIPLY, ExpressionValues::multiply);
      EVALUATORS.put(TokenType.DIVIDE, ExpressionValues::divide);
      EVALUATORS.put(TokenType.DIV, ExpressionValues::div);
      EVALUATORS.put(TokenType.MOD, ExpressionValues::mod);
      EVALUATORS.put(TokenType.SHL, ExpressionValues::shl);
      EVALUATORS.put(TokenType.SHR, ExpressionValues::shr);
      EVALUATORS.put(TokenType.EQUALS, ExpressionValues::isEqual);
      EVALUATORS.put(TokenType.GREATER_THAN, ExpressionValues::greaterThan);
      EVALUATORS.put(TokenType.LESS_THAN, ExpressionValues::lessThan);
      EVALUATORS.put(TokenType.GREATER_THAN_EQUAL, ExpressionValues::greaterThanEqual);
      EVALUATORS.put(TokenType.LESS_THAN_EQUAL, ExpressionValues::lessThanEqual);
      EVALUATORS.put(TokenType.NOT_EQUALS, ExpressionValues::notEqual);
      EVALUATORS.put(TokenType.IN, ExpressionValues::in);
      EVALUATORS.put(TokenType.AND, ExpressionValues::and);
      EVALUATORS.put(TokenType.OR, ExpressionValues::or);
      EVALUATORS.put(TokenType.XOR, ExpressionValues::xor);
    }

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
      BinaryEvaluator evaluator = Preconditions.checkNotNull(EVALUATORS.get(operator));
      Preconditions.checkNotNull(evaluator, "Unhandled binary operator '" + operator.name() + "'");
      ExpressionValue left = leftExpression.evaluate(preprocessor);
      ExpressionValue right = rightExpression.evaluate(preprocessor);

      return evaluator.apply(left, right);
    }
  }

  static final class UnaryExpression implements Expression {
    private static final Map<TokenType, UnaryEvaluator> EVALUATORS =
        Map.of(
            TokenType.PLUS, ExpressionValues::plus,
            TokenType.MINUS, ExpressionValues::negate,
            TokenType.NOT, ExpressionValues::not);

    private final TokenType operator;
    private final Expression expression;

    private UnaryExpression(TokenType operator, Expression expression) {
      this.operator = operator;
      this.expression = expression;
    }

    @Override
    public ExpressionValue evaluate(DelphiPreprocessor preprocessor) {
      UnaryEvaluator evaluator = EVALUATORS.get(operator);
      Preconditions.checkNotNull(evaluator, "Unhandled unary operator '" + operator.name() + "'");
      ExpressionValue value = expression.evaluate(preprocessor);

      return evaluator.apply(value);
    }
  }

  static final class LiteralExpression implements Expression {
    private final ExpressionValue value;

    private LiteralExpression(TokenType type, String text) {
      value = createValue(type, text);
    }

    private static BigInteger bigIntegerFromTextWithDigitSeparatorsAndRadixPrefix(String text) {
      int radix;
      switch (text.charAt(0)) {
        case '$':
          text = StringUtils.removeStart(text, "$");
          radix = 16;
          break;
        case '%':
          text = StringUtils.removeStart(text, "%");
          radix = 2;
          break;
        default:
          radix = 10;
      }
      String withoutUnderscores = StringUtils.remove(text, '_');
      return withoutUnderscores.isEmpty()
          ? BigInteger.ZERO
          : new BigInteger(withoutUnderscores, radix);
    }

    private static double doubleFromTextWithDigitSeparators(String text) {
      return Double.parseDouble(StringUtils.remove(text, '_'));
    }

    private static ExpressionValue createValue(TokenType type, String text) {
      switch (type) {
        case INTEGER:
          return createInteger(bigIntegerFromTextWithDigitSeparatorsAndRadixPrefix(text));
        case DECIMAL:
          return createDecimal(doubleFromTextWithDigitSeparators(text));
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

  static final class SetExpression implements Expression {
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

  static final class NameReferenceExpression implements Expression {
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

  static final class InvocationExpression implements Expression {
    private final String name;
    private final List<Expression> arguments;

    private InvocationExpression(String name, List<Expression> arguments) {
      this.name = name;
      this.arguments = arguments;
    }

    @Nullable
    private static IntrinsicType searchIntrinsicTypes(String name) {
      return Arrays.stream(IntrinsicType.values())
          .filter(
              intrinsic ->
                  intrinsic.simpleName().equalsIgnoreCase(name)
                      || intrinsic.fullyQualifiedName().equalsIgnoreCase(name))
          .findFirst()
          .orElse(null);
    }

    private static int sizeOf(DelphiPreprocessor preprocessor, Expression expression) {
      Type type = null;
      TypeFactory typeFactory = preprocessor.getTypeFactory();

      if (expression instanceof NameReferenceExpression) {
        IntrinsicType intrinsic = searchIntrinsicTypes(((NameReferenceExpression) expression).name);
        if (intrinsic != null) {
          type = typeFactory.getIntrinsic(intrinsic);
        }
      }

      if (type == null) {
        ExpressionValue value = expression.evaluate(preprocessor);
        switch (value.type()) {
          case STRING:
            type = typeFactory.getIntrinsic(IntrinsicType.STRING);
            break;
          case INTEGER:
            type = typeFactory.integerFromLiteralValue(value.asBigInteger());
            break;
          case DECIMAL:
            type = typeFactory.getIntrinsic(IntrinsicType.EXTENDED);
            break;
          case BOOLEAN:
            type = typeFactory.getIntrinsic(IntrinsicType.BOOLEAN);
            break;
          case SET:
            type = typeFactory.emptySet();
            break;
          default:
            // Do nothing
        }
      }

      if (type == null) {
        type = typeFactory.getIntrinsic(IntrinsicType.POINTER);
      }

      return type.size();
    }

    private boolean isIntrinsic(String intrinsic, int minArguments) {
      return StringUtils.removeStartIgnoreCase(name, "System.").equalsIgnoreCase(intrinsic)
          && arguments.size() >= minArguments;
    }

    @Override
    public ExpressionValue evaluate(DelphiPreprocessor preprocessor) {
      if (isIntrinsic("Defined", 1)) {
        Expression argument = arguments.get(0);
        if (argument instanceof NameReferenceExpression) {
          boolean isDefined = preprocessor.isDefined(((NameReferenceExpression) argument).name);
          return createBoolean(isDefined);
        }
      } else if (isIntrinsic("SizeOf", 1)) {
        int size = sizeOf(preprocessor, arguments.get(0));
        return createInteger(size);
      }
      return unknownValue();
    }
  }
}
