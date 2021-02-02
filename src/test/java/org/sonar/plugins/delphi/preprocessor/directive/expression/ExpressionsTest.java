package org.sonar.plugins.delphi.preprocessor.directive.expression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Expressions.binary;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Expressions.literal;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Expressions.unary;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.INTEGER;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.UNKNOWN;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.preprocessor.DelphiPreprocessor;
import org.sonar.plugins.delphi.preprocessor.directive.CompilerDirective.Expression;
import org.sonar.plugins.delphi.preprocessor.directive.CompilerDirective.Expression.ConstExpressionType;
import org.sonar.plugins.delphi.preprocessor.directive.CompilerDirective.Expression.ExpressionValue;
import org.sonar.plugins.delphi.type.factory.TypeFactory;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicType;
import org.sonar.plugins.delphi.utils.types.TypeFactoryUtils;

class ExpressionsTest {
  private static final TypeFactory TYPE_FACTORY = TypeFactoryUtils.defaultFactory();
  private DelphiPreprocessor preprocessor;
  private ExpressionEvaluator evaluator;

  @BeforeEach
  void setup() {
    preprocessor = mock(DelphiPreprocessor.class);
    when(preprocessor.getTypeFactory()).thenReturn(TYPE_FACTORY);
    evaluator = new ExpressionEvaluator(preprocessor);
  }

  @Test
  void testMathematicEvaluation() {
    assertInt("1 + 2", 3);
    assertInt("1 - 2", -1);
    assertInt("1 * 2", 2);
    assertInt("10 / 2", 5);
    assertInt("1 shl 2", 4);
    assertInt("12 shr 1", 6);

    assertUnknown("1 shl 1.0");
    assertUnknown("1 shr 1.0");

    assertDecimal("1 + 2", 3.0);
    assertDecimal("5 / 2", 2.5);
    assertDecimal("5 div 2", 2.0);
    assertDecimal("10 mod 3", 1.0);
    assertDecimal("5.0 / 2.0", 2.5);
  }

  @Test
  void testStringConcatenation() {
    assertString("'abc' + '123", "abc123");
    assertString("'abc' + '", "abc");
    assertUnknown("'abc' + 123");
  }

  @Test
  void testEqualityEvaluation() {
    assertBool("1 = 1", true);
    assertBool("1 = 2", false);
    assertBool("1 = 1.0", true);
    assertBool("1.0 = 1.0", true);
    assertBool("1.0 = 2.0", false);
    assertBool("'my string' = 'my string'", true);
    assertBool("'my string' = 'MY STRING'", false);
    assertBool("'1' = 1", false);
    assertBool("'1.0' = 1.0", false);
    assertBool("[1, 2, 3] = [1, 2, 3]", true);
    assertBool("[1, 2, 3] = [4, 5, 6]", false);
    assertUnknown("UNKNOWN = 5");

    assertBool("1 <> 1", false);
    assertBool("1 <> 2", true);
    assertBool("1 <> 1.0", false);
    assertBool("1.0 <> 1.0", false);
    assertBool("1.0 <> 2.0", true);
    assertBool("'my string' <> 'my string'", false);
    assertBool("'my string' <> 'MY STRING'", true);
    assertBool("'1' <> 1", true);
    assertBool("'1.0' <> 1.0", true);
    assertBool("[1, 2, 3] <> [1, 2, 3]", false);
    assertBool("[1, 2, 3] <> [4, 5, 6]", true);
    assertUnknown("UNKNOWN <> 5");
  }

  @Test
  void testComparisonEvaluation() {
    assertBool("1 > 1", false);
    assertBool("2 > 1", true);
    assertBool("1 > 1.0", false);
    assertBool("1.0 > 1.0", false);
    assertBool("2.0 > 1.0", true);
    assertUnknown("'my string' > 'my string'");

    assertBool("1 < 1", false);
    assertBool("2 < 1", false);
    assertBool("1 < 1.0", false);
    assertBool("1.0 < 1.0", false);
    assertBool("2.0 < 1.0", false);
    assertUnknown("'my string' < 'my string'");

    assertBool("1 >= 1", true);
    assertBool("2 >= 1", true);
    assertBool("1 >= 1.0", true);
    assertBool("1.0 >= 1.0", true);
    assertBool("2.0 >= 1.0", true);
    assertBool("[1, 2, 3] >= [1, 2, 3]", true);
    assertBool("[1, 2, 3, 4, 5, 6] >= [1, 2, 3]", true);
    assertBool("[1, 2, 3] >= [1, 2, 3, 4, 5, 6]", false);
    assertUnknown("'my string' >= 'my string'");

    assertBool("1 <= 1", true);
    assertBool("2 <= 1", false);
    assertBool("1 <= 1.0", true);
    assertBool("1.0 <= 1.0", true);
    assertBool("2.0 <= 1.0", false);
    assertBool("[1, 2, 3] <= [1, 2, 3]", true);
    assertBool("[1, 2, 3, 4, 5, 6] <= [1, 2, 3]", false);
    assertBool("[1, 2, 3] <= [1, 2, 3, 4, 5, 6]", true);
    assertUnknown("'my string' <= 'my string'");
  }

  @Test
  void testLogicalOperators() {
    assertBool("1 in [1, 2, 3]", true);
    assertBool("1 in [2, 3]", false);
    assertBool("1 in []", false);
    assertUnknown("1 in 1");

    assertBool("True and True", true);
    assertBool("True and False", false);
    assertBool("False and False", false);
    assertUnknown("True and 1");

    assertBool("True or True", true);
    assertBool("True or False", true);
    assertBool("False or False", false);
    assertUnknown("True or 1");

    assertBool("True xor True", false);
    assertBool("True xor False", true);
    assertBool("False xor True", true);
    assertBool("False xor False", false);
    assertUnknown("True xor 1");
  }

  @Test
  void testUnaryEvaluation() {
    assertInt("+1", 1);
    assertInt("-1", -1);

    assertDecimal("+1.0", 1.0);
    assertDecimal("-1.0", -1.0);

    assertBool("not True", false);
    assertBool("not False", true);

    assertUnknown("+'my string'");
    assertUnknown("+True");
    assertUnknown("not 1");
  }

  @Test
  void testDefinedEvaluation() {
    when(preprocessor.isDefined("TEST_DEFINE")).thenReturn(true);

    assertBool("Defined(TEST_DEFINE)", true);
    assertBool("Defined(NOT_DEFINED)", false);

    assertUnknown("Defined()");
    assertUnknown("Defined(123)");
  }

  private static int size(IntrinsicType type) {
    return TYPE_FACTORY.getIntrinsic(type).size();
  }

  @Test
  void testSizeOfEvaluation() {
    assertInt("SizeOf(Byte)", size(IntrinsicType.BYTE));
    assertInt("SizeOf(NativeInt)", size(IntrinsicType.NATIVEINT));
    assertInt("SizeOf(LongWord)", size(IntrinsicType.LONGWORD));
    assertInt("SizeOf(Double)", size(IntrinsicType.DOUBLE));
    assertInt("SizeOf(Boolean)", size(IntrinsicType.BOOLEAN));
    assertInt("SizeOf(String)", size(IntrinsicType.UNICODESTRING));
    assertInt("SizeOf(Pointer)", size(IntrinsicType.POINTER));
    assertInt("SizeOf(Variant)", size(IntrinsicType.VARIANT));
    assertInt("SizeOf(TObject)", size(IntrinsicType.POINTER));
    assertInt("SizeOf('Foo')", size(IntrinsicType.UNICODESTRING));
    assertInt("SizeOf(123)", size(IntrinsicType.BYTE));
    assertInt("SizeOf(123.456)", size(IntrinsicType.EXTENDED));
    assertInt("SizeOf(True)", size(IntrinsicType.BOOLEAN));
    assertInt("SizeOf([])", TYPE_FACTORY.emptySet().size());
    assertInt("SizeOf(String)", size(IntrinsicType.UNICODESTRING));
  }

  @Test
  void testSizeOfUnknownShouldFailOnUpgrade() {
    assertInt("SizeOf(SomeUnknownType)", size(IntrinsicType.POINTER));
  }

  @Test
  void testBinaryExpressionWithUnhandledOperatorShouldThrow() {
    Expression expression = binary(literal(INTEGER, "1"), UNKNOWN, literal(INTEGER, "1"));
    assertThatThrownBy(() -> expression.evaluate(preprocessor))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void testUnaryExpressionWithUnhandledOperatorShouldThrow() {
    Expression expression = unary(UNKNOWN, literal(INTEGER, "1"));
    assertThatThrownBy(() -> expression.evaluate(preprocessor))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void testUnhandledLiteralTypeShouldThrow() {
    assertThatThrownBy(() -> literal(UNKNOWN, "value")).isInstanceOf(AssertionError.class);
  }

  private void assertUnknown(String data) {
    assertThat(evaluator.evaluate(data).type()).isEqualTo(ConstExpressionType.UNKNOWN);
  }

  private void assertInt(String data, int expected) {
    assertThat(evaluator.evaluate(data).asInteger()).isEqualTo(expected);
  }

  private void assertDecimal(String data, double expected) {
    assertThat(evaluator.evaluate(data).asDecimal()).isEqualTo(expected);
  }

  private void assertString(String data, String expected) {
    assertThat(evaluator.evaluate(data).asString()).isEqualTo(expected);
  }

  private void assertBool(String data, boolean expected) {
    assertThat(evaluator.evaluate(data).asBoolean()).isEqualTo(expected);
  }

  private static class ExpressionEvaluator {
    private static final ExpressionLexer LEXER = new ExpressionLexer();
    private static final ExpressionParser PARSER = new ExpressionParser();
    private final DelphiPreprocessor preprocessor;

    ExpressionEvaluator(DelphiPreprocessor preprocessor) {
      this.preprocessor = preprocessor;
    }

    ExpressionValue evaluate(String data) {
      return PARSER.parse(LEXER.lex(data)).evaluate(preprocessor);
    }
  }
}
