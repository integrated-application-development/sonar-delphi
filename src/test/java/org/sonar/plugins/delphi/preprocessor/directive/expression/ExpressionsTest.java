package org.sonar.plugins.delphi.preprocessor.directive.expression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.delphi.preprocessor.directive.CompilerDirective.Expression.ConstExpressionType.UNKNOWN;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Expressions.binary;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Expressions.literal;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Expressions.unary;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.INTEGER;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.sonar.plugins.delphi.preprocessor.DelphiPreprocessor;
import org.sonar.plugins.delphi.preprocessor.directive.CompilerDirective.Expression;
import org.sonar.plugins.delphi.preprocessor.directive.CompilerDirective.Expression.ExpressionValue;
import org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType;
import org.sonar.plugins.delphi.type.factory.TypeFactory;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicType;
import org.sonar.plugins.delphi.utils.types.TypeFactoryUtils;

class ExpressionsTest {
  static class IntegerMathArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("1 + 2", 3),
          Arguments.of("1 - 2", -1),
          Arguments.of("1 * 2", 2),
          Arguments.of("10 / 2", 5),
          Arguments.of("1 shl 2", 4),
          Arguments.of("12 shr 1", 6),
          Arguments.of("1 shl 1.0", UNKNOWN),
          Arguments.of("1 shr 1.0", UNKNOWN),
          Arguments.of("1 + 'foo'", UNKNOWN),
          Arguments.of("'foo' + 1", UNKNOWN));
    }
  }

  static class DecimalMathArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("1 + 2", 3.0),
          Arguments.of("5 / 2", 2.5),
          Arguments.of("5 div 2", 2.0),
          Arguments.of("10 mod 3", 1.0),
          Arguments.of("5.0 / 2.0", 2.5),
          Arguments.of("1.0 shl 1", UNKNOWN),
          Arguments.of("1.0 shr 1", UNKNOWN));
    }
  }

  static class StringConcatenationArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("'abc' + '123", "abc123"),
          Arguments.of("'abc' + '", "abc"),
          Arguments.of("'abc' + 123", UNKNOWN));
    }
  }

  static class EqualityArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("1 = 1", true),
          Arguments.of("1 = 2", false),
          Arguments.of("1 = 1.0", true),
          Arguments.of("1.0 = 1.0", true),
          Arguments.of("1.0 = 2.0", false),
          Arguments.of("1.0 = '2.0'", false),
          Arguments.of("'1.0' = 2.0", false),
          Arguments.of("'my string' = 'my string'", true),
          Arguments.of("'my string' = 'MY STRING'", false),
          Arguments.of("'1' = 1", false),
          Arguments.of("'1.0' = 1.0", false),
          Arguments.of("[1, 2, 3] = [1, 2, 3]", true),
          Arguments.of("[1, 2, 3] = [4, 5, 6]", false),
          Arguments.of("UNKNOWN = 5", UNKNOWN),
          Arguments.of("5 = UNKNOWN", UNKNOWN),
          Arguments.of("UNKNOWN = UNKNOWN", UNKNOWN),
          Arguments.of("1 <> 1", false),
          Arguments.of("1 <> 2", true),
          Arguments.of("1 <> 1.0", false),
          Arguments.of("1.0 <> 1.0", false),
          Arguments.of("1.0 <> 2.0", true),
          Arguments.of("1.0 <> '2.0'", true),
          Arguments.of("'1.0' <> 2.0", true),
          Arguments.of("'my string' <> 'my string'", false),
          Arguments.of("'my string' <> 'MY STRING'", true),
          Arguments.of("'1' <> 1", true),
          Arguments.of("'1.0' <> 1.0", true),
          Arguments.of("[1, 2, 3] <> [1, 2, 3]", false),
          Arguments.of("[1, 2, 3] <> [4, 5, 6]", true),
          Arguments.of("UNKNOWN <> 5", UNKNOWN),
          Arguments.of("5 <> UNKNOWN", UNKNOWN),
          Arguments.of("UNKNOWN <> UNKNOWN", UNKNOWN));
    }
  }

  static class ComparisonArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("1 > 1", false),
          Arguments.of("2 > 1", true),
          Arguments.of("1 > 1.0", false),
          Arguments.of("1.0 > 1.0", false),
          Arguments.of("2.0 > 1.0", true),
          Arguments.of("'1.0' > 1.0", UNKNOWN),
          Arguments.of("2.0 > '1.0'", UNKNOWN),
          Arguments.of("'2.0' > '1.0'", UNKNOWN),
          Arguments.of("1 < 1", false),
          Arguments.of("1 < 2", true),
          Arguments.of("1 < 1.0", false),
          Arguments.of("1.0 < 1.0", false),
          Arguments.of("1.0 < 2.0", true),
          Arguments.of("'1.0' < 1.0", UNKNOWN),
          Arguments.of("2.0 < '1.0'", UNKNOWN),
          Arguments.of("'2.0' < '1.0'", UNKNOWN),
          Arguments.of("1 >= 1", true),
          Arguments.of("1 >= 2", false),
          Arguments.of("1 >= 1.0", true),
          Arguments.of("1.0 >= 1.0", true),
          Arguments.of("1.0 >= 2.0", false),
          Arguments.of("[1, 2, 3] >= [1, 2, 3]", true),
          Arguments.of("[1, 2, 3, 4, 5, 6] >= [1, 2, 3]", true),
          Arguments.of("[1, 2, 3] >= [1, 2, 3, 4, 5, 6]", false),
          Arguments.of("'1.0' >= 1.0", UNKNOWN),
          Arguments.of("2.0 >= '1.0'", UNKNOWN),
          Arguments.of("'2.0' >= '1.0'", UNKNOWN),
          Arguments.of("[1, 2, 3] >= '[1, 2, 3]'", UNKNOWN),
          Arguments.of("'[1, 2, 3]' >= [1, 2, 3]", UNKNOWN),
          Arguments.of("1 <= 1", true),
          Arguments.of("2 <= 1", false),
          Arguments.of("1 <= 1.0", true),
          Arguments.of("1.0 <= 1.0", true),
          Arguments.of("2.0 <= 1.0", false),
          Arguments.of("[1, 2, 3] <= [1, 2, 3]", true),
          Arguments.of("[1, 2, 3, 4, 5, 6] <= [1, 2, 3]", false),
          Arguments.of("[1, 2, 3] <= [1, 2, 3, 4, 5, 6]", true),
          Arguments.of("'1.0' <= 1.0", UNKNOWN),
          Arguments.of("2.0 <= '1.0'", UNKNOWN),
          Arguments.of("'2.0' <= '1.0'", UNKNOWN),
          Arguments.of("[1, 2, 3] <= '[1, 2, 3]'", UNKNOWN),
          Arguments.of("'[1, 2, 3]' <= [1, 2, 3]", UNKNOWN));
    }
  }

  static class LogicalOperatorsArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("1 in [1, 2, 3]", true),
          Arguments.of("1 in [2, 3]", false),
          Arguments.of("1 in []", false),
          Arguments.of("1 in 1", UNKNOWN),
          Arguments.of("True and True", true),
          Arguments.of("True and False", false),
          Arguments.of("False and False", false),
          Arguments.of("True and 1", UNKNOWN),
          Arguments.of("1 and True", UNKNOWN),
          Arguments.of("True or True", true),
          Arguments.of("False or True", true),
          Arguments.of("True or False", true),
          Arguments.of("False or False", false),
          Arguments.of("True or 1", UNKNOWN),
          Arguments.of("1 or True", UNKNOWN),
          Arguments.of("True xor True", false),
          Arguments.of("True xor False", true),
          Arguments.of("False xor True", true),
          Arguments.of("False xor False", false),
          Arguments.of("True xor 1", UNKNOWN),
          Arguments.of("1 xor True", UNKNOWN));
    }
  }

  static class UnaryEvaluationArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("+1", 1),
          Arguments.of("-1", -1),
          Arguments.of("+1.0", 1.0),
          Arguments.of("-1.0", -1.0),
          Arguments.of("not True", false),
          Arguments.of("not False", true),
          Arguments.of("+'my string'", UNKNOWN),
          Arguments.of("+True", UNKNOWN),
          Arguments.of("not 1", UNKNOWN));
    }
  }

  static class DefinedArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("Defined(TEST_DEFINE)", true),
          Arguments.of("Defined(NOT_DEFINED)", false),
          Arguments.of("Defined()", UNKNOWN),
          Arguments.of("Defined(123)", UNKNOWN));
    }
  }

  static class SizeOfArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("SizeOf(Byte)", size(IntrinsicType.BYTE)),
          Arguments.of("SizeOf(NativeInt)", size(IntrinsicType.NATIVEINT)),
          Arguments.of("SizeOf(LongWord)", size(IntrinsicType.LONGWORD)),
          Arguments.of("SizeOf(Double)", size(IntrinsicType.DOUBLE)),
          Arguments.of("SizeOf(Boolean)", size(IntrinsicType.BOOLEAN)),
          Arguments.of("SizeOf(String)", size(IntrinsicType.UNICODESTRING)),
          Arguments.of("SizeOf(Pointer)", size(IntrinsicType.POINTER)),
          Arguments.of("SizeOf(Variant)", size(IntrinsicType.VARIANT)),
          Arguments.of("SizeOf(TObject)", size(IntrinsicType.POINTER)),
          Arguments.of("SizeOf('Foo')", size(IntrinsicType.UNICODESTRING)),
          Arguments.of("SizeOf(123)", size(IntrinsicType.BYTE)),
          Arguments.of("SizeOf(123.456)", size(IntrinsicType.EXTENDED)),
          Arguments.of("SizeOf(True)", size(IntrinsicType.BOOLEAN)),
          Arguments.of("SizeOf([])", TYPE_FACTORY.emptySet().size()),
          Arguments.of("SizeOf(String)", size(IntrinsicType.UNICODESTRING)));
    }
  }

  private static final TypeFactory TYPE_FACTORY = TypeFactoryUtils.defaultFactory();
  private DelphiPreprocessor preprocessor;

  @BeforeEach
  void setup() {
    preprocessor = mock(DelphiPreprocessor.class);
    when(preprocessor.getTypeFactory()).thenReturn(TYPE_FACTORY);
    when(preprocessor.isDefined("TEST_DEFINE")).thenReturn(true);
  }

  @ParameterizedTest(name = "\"{0}\" should evaluate to: {1}")
  @ArgumentsSource(IntegerMathArgumentsProvider.class)
  @ArgumentsSource(DecimalMathArgumentsProvider.class)
  @ArgumentsSource(StringConcatenationArgumentsProvider.class)
  @ArgumentsSource(EqualityArgumentsProvider.class)
  @ArgumentsSource(ComparisonArgumentsProvider.class)
  @ArgumentsSource(LogicalOperatorsArgumentsProvider.class)
  @ArgumentsSource(UnaryEvaluationArgumentsProvider.class)
  @ArgumentsSource(DefinedArgumentsProvider.class)
  @ArgumentsSource(SizeOfArgumentsProvider.class)
  void testExpressionEvaluation(String input, Object expected) {
    assertValue(input, expected);
  }

  @Test
  void testSizeOfUnknownShouldFailOnUpgrade() {
    assertValue("SizeOf(SomeUnknownType)", size(IntrinsicType.POINTER));
  }

  @Test
  void testBinaryExpressionWithUnhandledOperatorShouldThrow() {
    Expression expression = binary(literal(INTEGER, "1"), TokenType.UNKNOWN, literal(INTEGER, "1"));
    assertThatThrownBy(() -> expression.evaluate(preprocessor))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void testUnaryExpressionWithUnhandledOperatorShouldThrow() {
    Expression expression = unary(TokenType.UNKNOWN, literal(INTEGER, "1"));
    assertThatThrownBy(() -> expression.evaluate(preprocessor))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void testUnhandledLiteralTypeShouldThrow() {
    assertThatThrownBy(() -> literal(TokenType.UNKNOWN, "value"))
        .isInstanceOf(AssertionError.class);
  }

  private void assertValue(String data, Object expected) {
    ExpressionLexer lexer = new ExpressionLexer();
    List<Token> tokens = lexer.lex(data);

    ExpressionParser parser = new ExpressionParser();
    Expression expression = parser.parse(tokens);

    ExpressionValue value = expression.evaluate(preprocessor);

    if (expected == UNKNOWN) {
      assertThat(value.type()).isEqualTo(UNKNOWN);
    } else if (expected instanceof String) {
      assertThat(value.asString()).isEqualTo(expected);
    } else if (expected instanceof Integer) {
      assertThat(value.asInteger()).isEqualTo(expected);
    } else if (expected instanceof Double) {
      assertThat(value.asDecimal()).isEqualTo(expected);
    } else if (expected instanceof Boolean) {
      assertThat(value.asBoolean()).isEqualTo(expected);
    }
  }

  private static int size(IntrinsicType type) {
    return TYPE_FACTORY.getIntrinsic(type).size();
  }
}
