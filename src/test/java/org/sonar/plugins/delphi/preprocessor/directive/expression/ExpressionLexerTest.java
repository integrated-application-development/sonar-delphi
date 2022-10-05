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
package org.sonar.plugins.delphi.preprocessor.directive.expression;

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
import org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionLexer.ExpressionLexerError;
import org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType;

class ExpressionLexerTest {
  static class NumberTokensArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("12345", TokenType.INTEGER),
          Arguments.of("123_456_789", TokenType.INTEGER),
          Arguments.of("%01101011", TokenType.INTEGER),
          Arguments.of("$014FE89", TokenType.INTEGER),
          Arguments.of("123.45", TokenType.DECIMAL),
          Arguments.of("5E5", TokenType.DECIMAL),
          Arguments.of("5E+5", TokenType.DECIMAL),
          Arguments.of("5E-5", TokenType.DECIMAL),
          Arguments.of("123_45.6_789", TokenType.DECIMAL),
          Arguments.of("12345____.6__78_9", TokenType.DECIMAL),
          Arguments.of("     123.45      ", TokenType.DECIMAL));
    }
  }

  static class IdentifierTokensArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("MyIdentifier", TokenType.IDENTIFIER),
          Arguments.of("_MyIdentifier", TokenType.IDENTIFIER),
          Arguments.of("True", TokenType.IDENTIFIER),
          Arguments.of("False", TokenType.IDENTIFIER));
    }
  }

  static class SyntaxTokensArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(".", TokenType.DOT),
          Arguments.of(",", TokenType.COMMA),
          Arguments.of("(", TokenType.LPAREN),
          Arguments.of(")", TokenType.RPAREN),
          Arguments.of("[", TokenType.LBRACKET),
          Arguments.of("]", TokenType.RBRACKET),
          Arguments.of("(.", TokenType.LBRACKET),
          Arguments.of(".)", TokenType.RBRACKET));
    }
  }

  static class OperatorTokensArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("<>", TokenType.NOT_EQUALS),
          Arguments.of("<=", TokenType.LESS_THAN_EQUAL),
          Arguments.of(">=", TokenType.GREATER_THAN_EQUAL),
          Arguments.of("=", TokenType.EQUALS));
    }
  }

  static class StringTokensArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("'My string'", TokenType.STRING),
          Arguments.of("'Escaped '' single-quotes'", TokenType.STRING));
    }
  }

  @ParameterizedTest(name = "\"{0}\" should be token type: {1}")
  @ArgumentsSource(NumberTokensArgumentsProvider.class)
  @ArgumentsSource(IdentifierTokensArgumentsProvider.class)
  @ArgumentsSource(SyntaxTokensArgumentsProvider.class)
  @ArgumentsSource(OperatorTokensArgumentsProvider.class)
  @ArgumentsSource(StringTokensArgumentsProvider.class)
  void testTokenTypes(String data, TokenType type) {
    assertThat(lexToken(data).getType()).isEqualTo(type);
  }

  @ParameterizedTest(name = "\"{0}\" should throw a lexer error")
  @ValueSource(strings = {"123.45.67", "#", "%001101.00101", "$FE.123", "123E$FE", "123E%011101"})
  void testInvalidTokens(String data) {
    assertThatThrownBy(() -> lexToken(data)).isInstanceOf(ExpressionLexerError.class);
  }

  private static Token lexToken(String data) {
    List<Token> tokens = new ExpressionLexer().lex(data);
    assertThat(tokens).hasSize(1);
    return tokens.get(0);
  }
}
