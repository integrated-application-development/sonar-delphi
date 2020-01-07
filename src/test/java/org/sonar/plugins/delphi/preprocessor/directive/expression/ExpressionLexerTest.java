package org.sonar.plugins.delphi.preprocessor.directive.expression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.Test;
import org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionLexer.ExpressionLexerError;
import org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType;

public class ExpressionLexerTest {
  private static final ExpressionLexer LEXER = new ExpressionLexer();

  @Test
  public void testNumbers() {
    assertThat(lexToken("12345").getType()).isEqualTo(TokenType.INTEGER);
    assertThat(lexToken("123.45").getType()).isEqualTo(TokenType.DECIMAL);
    assertThat(lexToken("     123.45      ").getType()).isEqualTo(TokenType.DECIMAL);
    assertThatThrownBy(() -> lexToken("123.45.67")).isInstanceOf(ExpressionLexerError.class);
  }

  @Test
  public void testIdentifier() {
    assertThat(lexToken("MyIdentifier").getType()).isEqualTo(TokenType.IDENTIFIER);
    assertThat(lexToken("_MyIdentifier").getType()).isEqualTo(TokenType.IDENTIFIER);
    assertThat(lexToken("True").getType()).isEqualTo(TokenType.IDENTIFIER);
    assertThat(lexToken("False").getType()).isEqualTo(TokenType.IDENTIFIER);
  }

  @Test
  public void testOperator() {
    assertThat(lexToken("<>").getType()).isEqualTo(TokenType.NOT_EQUALS);
    assertThat(lexToken("<=").getType()).isEqualTo(TokenType.LESS_THAN_EQUAL);
    assertThat(lexToken(">=").getType()).isEqualTo(TokenType.GREATER_THAN_EQUAL);
    assertThat(lexToken("=").getType()).isEqualTo(TokenType.EQUALS);
  }

  @Test
  public void testString() {
    assertThat(lexToken("'My string'").getType()).isEqualTo(TokenType.STRING);
    assertThat(lexToken("'Escaped '' single-quotes'").getType()).isEqualTo(TokenType.STRING);
  }

  @Test
  public void testInvalidToken() {
    assertThatThrownBy(() -> lexToken("#")).isInstanceOf(ExpressionLexerError.class);
  }

  private static Token lexToken(String data) {
    List<Token> tokens = LEXER.lex(data);
    assertThat(tokens).hasSize(1);
    return tokens.get(0);
  }
}
