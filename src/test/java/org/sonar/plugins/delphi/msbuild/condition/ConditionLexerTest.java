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
package org.sonar.plugins.delphi.msbuild.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.plugins.delphi.msbuild.condition.ConditionLexer.ConditionLexerError;
import org.sonar.plugins.delphi.msbuild.condition.Token.TokenType;

class ConditionLexerTest {
  static class NumberTokensArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("123", TokenType.NUMERIC),
          Arguments.of("+123", TokenType.NUMERIC),
          Arguments.of("-123", TokenType.NUMERIC),
          Arguments.of(".123", TokenType.NUMERIC),
          Arguments.of("123.45", TokenType.NUMERIC),
          Arguments.of("123.45.67", TokenType.NUMERIC),
          Arguments.of("123.45.67.89", TokenType.NUMERIC),
          Arguments.of("0123", TokenType.NUMERIC),
          Arguments.of("0x1A", TokenType.NUMERIC),
          Arguments.of("0x9f", TokenType.NUMERIC));
    }
  }

  static class SyntaxTokensArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(",", TokenType.COMMA),
          Arguments.of("(", TokenType.LPAREN),
          Arguments.of(")", TokenType.RPAREN));
    }
  }

  static class OperatorTokensArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("<", TokenType.LESS_THAN),
          Arguments.of(">", TokenType.GREATER_THAN),
          Arguments.of("<=", TokenType.LESS_THAN_EQUAL),
          Arguments.of(">=", TokenType.GREATER_THAN_EQUAL),
          Arguments.of("and", TokenType.AND),
          Arguments.of("or", TokenType.OR),
          Arguments.of("==", TokenType.EQUAL),
          Arguments.of("!=", TokenType.NOT_EQUAL),
          Arguments.of("!", TokenType.NOT));
    }
  }

  static class StringTokensArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("'My quoted string'", TokenType.STRING),
          Arguments.of("'$1.00'", TokenType.STRING),
          Arguments.of("SimpleString", TokenType.STRING),
          Arguments.of("True", TokenType.STRING),
          Arguments.of("False", TokenType.STRING),
          Arguments.of("_Foo", TokenType.STRING),
          Arguments.of("SimpleStringContainingNumbers123", TokenType.STRING));
    }
  }

  static class PropertyTokensArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("$(FOO)", TokenType.PROPERTY), Arguments.of("'$(FOO)'", TokenType.STRING));
    }
  }

  @ParameterizedTest(name = "\"{0}\" should be token type: {1}")
  @ArgumentsSource(NumberTokensArgumentsProvider.class)
  @ArgumentsSource(SyntaxTokensArgumentsProvider.class)
  @ArgumentsSource(OperatorTokensArgumentsProvider.class)
  @ArgumentsSource(StringTokensArgumentsProvider.class)
  @ArgumentsSource(PropertyTokensArgumentsProvider.class)
  void testTokenTypes(String data, TokenType type) {
    assertThat(lexToken(data).getType()).isEqualTo(type);
  }

  @ParameterizedTest(name = "\"{0}\" should throw a lexer error")
  @ValueSource(
      strings = {
        "~",
        "^",
        "#",
        "$",
        "&",
        "*",
        "?",
        "{",
        "}",
        "'string without closing quote",
        "$(FOO$(BAR)"
      })
  void testInvalidTokens(String data) {
    assertThatThrownBy(() -> lexToken(data)).isInstanceOf(ConditionLexerError.class);
  }

  @ParameterizedTest(name = "\"{0}\" should contain multiple tokens")
  @ValueSource(
      strings = {
        "0x123G", "0x123g", "0x123>", "0x123_",
      })
  void testMultipleTokens(String data) {
    List<Token> tokens = new ConditionLexer().lex(data);
    assertThat(tokens).hasSizeGreaterThan(1);
  }

  private static Token lexToken(String data) {
    List<Token> tokens = new ConditionLexer().lex(data);
    assertThat(tokens).hasSize(1);
    return tokens.get(0);
  }
}
