package org.sonar.plugins.delphi.preprocessor.directive.expression;

public class Token {
  enum TokenType {
    UNKNOWN,
    INTEGER,
    DECIMAL,
    IDENTIFIER,
    STRING,
    EQUALS,
    NOT_EQUALS,
    LESS_THAN,
    GREATER_THAN,
    LESS_THAN_EQUAL,
    GREATER_THAN_EQUAL,
    DIV,
    MOD,
    SHL,
    SHR,
    IN,
    NOT,
    AND,
    OR,
    XOR,
    LPAREN,
    RPAREN,
    LBRACKET,
    RBRACKET,
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    COMMA,
    DOT
  }

  private final TokenType type;
  private final String text;

  public Token(TokenType type, String text) {
    this.type = type;
    this.text = text;
  }

  public TokenType getType() {
    return type;
  }

  public String getText() {
    return text;
  }
}
