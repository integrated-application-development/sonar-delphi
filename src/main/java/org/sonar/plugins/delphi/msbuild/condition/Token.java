package org.sonar.plugins.delphi.msbuild.condition;

public class Token {
  enum TokenType {
    COMMA,
    LPAREN,
    RPAREN,
    LESS_THAN,
    GREATER_THAN,
    LESS_THAN_EQUAL,
    GREATER_THAN_EQUAL,
    AND,
    OR,
    EQUAL,
    NOT_EQUAL,
    NOT,
    PROPERTY,
    STRING,
    NUMERIC,
    FUNCTION,
    END_OF_INPUT
  }

  private final TokenType type;
  private final String text;
  private final boolean expandable;

  public Token(TokenType type, String text, boolean expandable) {
    this.type = type;
    this.text = text;
    this.expandable = expandable;
  }

  public Token(TokenType type, String text) {
    this(type, text, false);
  }

  public TokenType getType() {
    return type;
  }

  public String getText() {
    return text;
  }

  public boolean getExpandable() {
    return expandable;
  }
}
