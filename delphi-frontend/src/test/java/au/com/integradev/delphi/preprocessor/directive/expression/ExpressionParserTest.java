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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import au.com.integradev.delphi.preprocessor.TextBlockLineEndingMode;
import au.com.integradev.delphi.preprocessor.directive.expression.ExpressionParser.ExpressionParserError;
import au.com.integradev.delphi.preprocessor.directive.expression.Expressions.BinaryExpression;
import au.com.integradev.delphi.preprocessor.directive.expression.Expressions.InvocationExpression;
import au.com.integradev.delphi.preprocessor.directive.expression.Expressions.LiteralExpression;
import au.com.integradev.delphi.preprocessor.directive.expression.Expressions.NameReferenceExpression;
import au.com.integradev.delphi.preprocessor.directive.expression.Expressions.SetExpression;
import au.com.integradev.delphi.preprocessor.directive.expression.Expressions.UnaryExpression;
import org.junit.jupiter.api.Test;

class ExpressionParserTest {
  private static final ExpressionLexer LEXER = new ExpressionLexer();
  private static final ExpressionParser PARSER = new ExpressionParser(TextBlockLineEndingMode.CRLF);

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
    assertThat(parse("'''\nMy\nmultiline\nstring\n'''")).isInstanceOf(LiteralExpression.class);
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
