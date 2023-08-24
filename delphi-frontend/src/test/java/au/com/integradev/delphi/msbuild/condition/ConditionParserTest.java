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
package au.com.integradev.delphi.msbuild.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import au.com.integradev.delphi.msbuild.condition.ConditionParser.ConditionParserError;
import au.com.integradev.delphi.msbuild.condition.Token.TokenType;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConditionParserTest {
  @Test
  void testBinary() {
    assertThat(parse("true and false")).isInstanceOf(BinaryExpression.class);
    assertThat(parse("true or false")).isInstanceOf(BinaryExpression.class);
    assertThat(parse("'foo' == 'bar'")).isInstanceOf(BinaryExpression.class);
    assertThat(parse("'foo' != 'bar'")).isInstanceOf(BinaryExpression.class);
  }

  @Test
  void testNot() {
    assertThat(parse("!'foo'")).isInstanceOf(NotExpression.class);
    assertThat(parse("!(true)")).isInstanceOf(NotExpression.class);
    assertThatThrownBy(() -> parse("!")).isInstanceOf(ConditionParserError.class);
  }

  @Test
  void testString() {
    assertThat(parse("'Quoted string'")).isInstanceOf(StringExpression.class);
    assertThat(parse("foo")).isInstanceOf(StringExpression.class);
    assertThat(parse("$(FOO)")).isInstanceOf(StringExpression.class);
  }

  @Test
  void testNumeric() {
    assertThat(parse("123")).isInstanceOf(NumericExpression.class);
    assertThat(parse("123.45")).isInstanceOf(NumericExpression.class);
    assertThat(parse("123.45.67")).isInstanceOf(NumericExpression.class);
    assertThat(parse("123.45.67.89")).isInstanceOf(NumericExpression.class);
    assertThat(parse("0x3c")).isInstanceOf(NumericExpression.class);
  }

  @Test
  void testFunction() {
    assertThat(parse("Exists('./foo/bar')")).isInstanceOf(FunctionCallExpression.class);
    assertThat(parse("Foo()")).isInstanceOf(FunctionCallExpression.class);
    assertThatThrownBy(() -> parse("HasTrailingSlash('./foo/bar'"))
        .isInstanceOf(ConditionParserError.class);
    assertThatThrownBy(
            () -> {
              ConditionParser parser = new ConditionParser();
              parser.parse(List.of(new Token(TokenType.FUNCTION, "HasTrailingSlash")));
            })
        .isInstanceOf(ConditionParserError.class);
  }

  @Test
  void testParenthesizedExpression() {
    assertThat(parse("(true and false)")).isInstanceOf(BinaryExpression.class);
    assertThatThrownBy(() -> parse("(true and false")).isInstanceOf(ConditionParserError.class);
  }

  @Test
  void testNoExpressionShouldThrowException() {
    assertThatThrownBy(() -> parse("and")).isInstanceOf(ConditionParserError.class);
  }

  @Test
  void testUnexpectedTrailingTokenShouldThrowException() {
    assertThatThrownBy(() -> parse("foo bar")).isInstanceOf(ConditionParserError.class);
  }

  private static Expression parse(String data) {
    ConditionLexer lexer = new ConditionLexer();
    List<Token> tokens = lexer.lex(data);

    ConditionParser parser = new ConditionParser();
    return parser.parse(tokens);
  }
}
