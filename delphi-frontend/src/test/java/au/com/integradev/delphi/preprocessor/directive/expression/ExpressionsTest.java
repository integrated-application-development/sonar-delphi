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

import static au.com.integradev.delphi.preprocessor.directive.expression.Expression.ConstExpressionType.UNKNOWN;
import static au.com.integradev.delphi.preprocessor.directive.expression.Expressions.binary;
import static au.com.integradev.delphi.preprocessor.directive.expression.Expressions.literal;
import static au.com.integradev.delphi.preprocessor.directive.expression.Expressions.unary;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.INTEGER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.compiler.CompilerVersion;
import au.com.integradev.delphi.preprocessor.DelphiPreprocessor;
import au.com.integradev.delphi.preprocessor.TextBlockLineEndingMode;
import au.com.integradev.delphi.preprocessor.directive.expression.Expression.ExpressionValue;
import au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

class ExpressionsTest {

  static class NumericExpressionArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("$26E", 622),
          Arguments.of("%10011_01110", 622),
          Arguments.of("5E2", 500),
          Arguments.of("5E+2", 500),
          Arguments.of("5E-2", .05),
          Arguments.of("123_456_789", 123456789),
          Arguments.of("123_45.6_789", 12345.6789));
    }
  }

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

  static class RealMathArgumentsProvider implements ArgumentsProvider {
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

  static class StringArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("'My string'", "My string"),
          Arguments.of("'Escaped '' single-quotes'", "Escaped ' single-quotes"),
          Arguments.of("'''\nMy\nmultiline\nstring\n'''", "My\r\nmultiline\r\nstring"),
          Arguments.of("'''''\nMy\nmultiline\nstring\n'''''", "My\r\nmultiline\r\nstring"));
    }
  }

  static class StringConcatenationArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("'abc' + '123'", "abc123"),
          Arguments.of("'abc' + ''", "abc"),
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
          Arguments.of("% = 0", true),
          Arguments.of("$ = 0", true),
          Arguments.of("%_ = 0", true),
          Arguments.of("$_ = 0", true),
          Arguments.of("_ = 0", UNKNOWN),
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
          Arguments.of("True and System.True", true),
          Arguments.of("True and False", false),
          Arguments.of("False and System.False", false),
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
          Arguments.of("not System.True", false),
          Arguments.of("not System.False", true),
          Arguments.of("+'my string'", UNKNOWN),
          Arguments.of("+True", UNKNOWN),
          Arguments.of("not 1", UNKNOWN));
    }
  }

  static class DefinedArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return addSystemQualifier(
          Stream.of(
              Arguments.of("Defined(TEST_DEFINE)", true),
              Arguments.of("Defined(NOT_DEFINED)", false),
              Arguments.of("Defined()", UNKNOWN),
              Arguments.of("Defined(123)", UNKNOWN)));
    }
  }

  static class SizeOfArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return addSystemQualifier(
          Stream.of(
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
              Arguments.of("SizeOf(String)", size(IntrinsicType.UNICODESTRING))));
    }
  }

  static class CompilerVersionArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return addSystemQualifier(Stream.of(Arguments.of("CompilerVersion", 30.0)));
    }
  }

  private static final TypeFactory TYPE_FACTORY = TypeFactoryUtils.defaultFactory();
  private DelphiPreprocessor preprocessor;

  @BeforeEach
  void setup() {
    preprocessor = mock(DelphiPreprocessor.class);
    when(preprocessor.getTypeFactory()).thenReturn(TYPE_FACTORY);
    when(preprocessor.isDefined("TEST_DEFINE")).thenReturn(true);
    when(preprocessor.getCompilerVersion()).thenReturn(CompilerVersion.fromVersionNumber("30.0"));
  }

  @ParameterizedTest(name = "\"{0}\" should evaluate to: {1}")
  @ArgumentsSource(NumericExpressionArgumentsProvider.class)
  @ArgumentsSource(IntegerMathArgumentsProvider.class)
  @ArgumentsSource(RealMathArgumentsProvider.class)
  @ArgumentsSource(StringArgumentsProvider.class)
  @ArgumentsSource(StringConcatenationArgumentsProvider.class)
  @ArgumentsSource(EqualityArgumentsProvider.class)
  @ArgumentsSource(ComparisonArgumentsProvider.class)
  @ArgumentsSource(LogicalOperatorsArgumentsProvider.class)
  @ArgumentsSource(UnaryEvaluationArgumentsProvider.class)
  @ArgumentsSource(DefinedArgumentsProvider.class)
  @ArgumentsSource(SizeOfArgumentsProvider.class)
  @ArgumentsSource(CompilerVersionArgumentsProvider.class)
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

    ExpressionParser parser = new ExpressionParser(TextBlockLineEndingMode.CRLF);
    Expression expression = parser.parse(tokens);

    ExpressionValue value = expression.evaluate(preprocessor);

    if (expected == UNKNOWN) {
      assertThat(value.type()).isEqualTo(UNKNOWN);
    } else if (expected instanceof String) {
      assertThat(value.asString()).isEqualTo(expected);
    } else if (expected instanceof Integer) {
      assertThat(value.asInteger()).isEqualTo(expected);
    } else if (expected instanceof Double) {
      assertThat(value.asDouble()).isEqualTo(expected);
    } else if (expected instanceof Boolean) {
      assertThat(value.asBoolean()).isEqualTo(expected);
    }
  }

  private static int size(IntrinsicType type) {
    return TYPE_FACTORY.getIntrinsic(type).size();
  }

  private static Stream<Arguments> addSystemQualifier(Stream<Arguments> arguments) {
    return arguments
        .map(
            arg ->
                List.of(
                    arg,
                    Arguments.of("System." + arg.get()[0], arg.get()[1]),
                    Arguments.of("System  .  " + arg.get()[0], arg.get()[1])))
        .flatMap(List::stream);
  }
}
