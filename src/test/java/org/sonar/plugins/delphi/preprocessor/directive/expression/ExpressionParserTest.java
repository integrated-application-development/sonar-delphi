package org.sonar.plugins.delphi.preprocessor.directive.expression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.preprocessor.directive.CompilerDirective.Expression;
import org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionParser.ExpressionParserError;
import org.sonar.plugins.delphi.preprocessor.directive.expression.Expressions.BinaryExpression;
import org.sonar.plugins.delphi.preprocessor.directive.expression.Expressions.InvocationExpression;
import org.sonar.plugins.delphi.preprocessor.directive.expression.Expressions.LiteralExpression;
import org.sonar.plugins.delphi.preprocessor.directive.expression.Expressions.NameReferenceExpression;
import org.sonar.plugins.delphi.preprocessor.directive.expression.Expressions.SetExpression;
import org.sonar.plugins.delphi.preprocessor.directive.expression.Expressions.UnaryExpression;

class ExpressionParserTest {
  private static final ExpressionLexer LEXER = new ExpressionLexer();
  private static final ExpressionParser PARSER = new ExpressionParser();

  @Test
  void testRelational() {
    assertThat(parse("1 > 2")).isInstanceOf(BinaryExpression.class);
    assertThat(parse("1 > 3 = False")).isInstanceOf(BinaryExpression.class);
  }

  @Test
  void testAddition() {
    assertThat(parse("1 + 2")).isInstanceOf(BinaryExpression.class);
    assertThat(parse("1 + 2 + 3")).isInstanceOf(BinaryExpression.class);
  }

  @Test
  void testMultiplication() {
    assertThat(parse("1 * 2")).isInstanceOf(BinaryExpression.class);
    assertThat(parse("1 * 2 / 3")).isInstanceOf(BinaryExpression.class);
  }

  @Test
  void testUnary() {
    assertThat(parse("not True")).isInstanceOf(UnaryExpression.class);
    assertThat(parse("not not True")).isInstanceOf(UnaryExpression.class);
  }

  @Test
  void testLiterals() {
    assertThat(parse("'My string'")).isInstanceOf(LiteralExpression.class);
    assertThat(parse("123")).isInstanceOf(LiteralExpression.class);
    assertThat(parse("123.45")).isInstanceOf(LiteralExpression.class);
  }

  @Test
  void testNameReference() {
    assertThat(parse("Identifier")).isInstanceOf(NameReferenceExpression.class);
    assertThat(parse("Qualified.Identifier")).isInstanceOf(NameReferenceExpression.class);
  }

  @Test
  void testInvocation() {
    assertThat(parse("SomeInvocation()")).isInstanceOf(InvocationExpression.class);
    assertThat(parse("SomeInvocation(1, 2, 3)")).isInstanceOf(InvocationExpression.class);
    assertThat(parse("Defined(MY_DEFINITION)")).isInstanceOf(InvocationExpression.class);
    assertThatThrownBy(() -> parse("SomeInvocation(1 X)"))
        .isInstanceOf(ExpressionParserError.class);
  }

  @Test
  void testSetExpression() {
    assertThat(parse("[]")).isInstanceOf(SetExpression.class);
    assertThat(parse("[0, 1, 2, 3, 4, 5]")).isInstanceOf(SetExpression.class);
    assertThatThrownBy(() -> parse("[0, 1, 2, 3 X, 5]")).isInstanceOf(ExpressionParserError.class);
  }

  @Test
  void testSubExpression() {
    assertThat(parse("(1 + 2)")).isInstanceOf(BinaryExpression.class);
    assertThatThrownBy(() -> parse("(1 + 2 X")).isInstanceOf(ExpressionParserError.class);
  }

  @Test
  void testNoExpressionShouldThrowException() {
    assertThatThrownBy(() -> parse("and")).isInstanceOf(ExpressionParserError.class);
  }

  private static Expression parse(String data) {
    return PARSER.parse(LEXER.lex(data));
  }
}
