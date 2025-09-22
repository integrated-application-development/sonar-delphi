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

public class Token {
  public enum TokenType {
    UNKNOWN,
    INTEGER,
    REAL,
    IDENTIFIER,
    STRING,
    MULTILINE_STRING,
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
    DOT,
    COMMENT,
    DIRECTIVE
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

  @Override
  public String toString() {
    return "Token{type=" + type + ", text='" + text + "'}";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Token token = (Token) obj;
    return type == token.type && java.util.Objects.equals(text, token.text);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(type, text);
  }
}
