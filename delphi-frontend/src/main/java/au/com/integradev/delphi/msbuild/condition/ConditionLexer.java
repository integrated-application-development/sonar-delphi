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

import au.com.integradev.delphi.msbuild.condition.Token.TokenType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class ConditionLexer {
  public static class ConditionLexerError extends RuntimeException {
    ConditionLexerError(String message) {
      super(message);
    }
  }

  private static final Map<Character, TokenType> SYNTAX_CHARACTERS =
      Map.of(
          ',', TokenType.COMMA,
          '(', TokenType.LPAREN,
          ')', TokenType.RPAREN);

  private static final Map<String, TokenType> OPERATOR_CHARACTERS =
      Map.of(
          "==", TokenType.EQUAL,
          "!=", TokenType.NOT_EQUAL,
          "!", TokenType.NOT,
          "<", TokenType.LESS_THAN,
          ">", TokenType.GREATER_THAN,
          "<=", TokenType.LESS_THAN_EQUAL,
          ">=", TokenType.GREATER_THAN_EQUAL);

  private static final Set<Character> OPERATOR_START =
      OPERATOR_CHARACTERS.keySet().stream()
          .map(key -> key.charAt(0))
          .collect(Collectors.toUnmodifiableSet());

  private static final char END_OF_INPUT = '\0';
  private String data;
  private int position;

  public List<Token> lex(String data) {
    this.data = data;
    this.position = 0;

    List<Token> result = new ArrayList<>();
    Token token;

    while ((token = readToken()) != null) {
      result.add(token);
    }

    return result;
  }

  private char peekChar() {
    if (position < data.length()) {
      return data.charAt(position);
    }
    return END_OF_INPUT;
  }

  private char getChar() {
    char character = peekChar();
    if (character != END_OF_INPUT) {
      ++position;
    }
    return character;
  }

  private void skipWhitespace() {
    char character;
    while ((character = peekChar()) != END_OF_INPUT) {
      if (!Character.isWhitespace(character)) {
        break;
      }
      ++position;
    }
  }

  @Nullable
  private Token readToken() {
    skipWhitespace();
    char character = peekChar();

    if (character != END_OF_INPUT) {
      if (OPERATOR_START.contains(character)) {
        return readOperator();
      } else if (SYNTAX_CHARACTERS.containsKey(character)) {
        return readSyntaxToken();
      } else if (character == '$') {
        return readProperty();
      } else if (character == '\'') {
        return readQuotedString();
      } else if (isNumericStart(character)) {
        return readNumeric();
      } else if (isSimpleStringStart(character)) {
        return readSimpleStringOrFunction();
      } else {
        throw new ConditionLexerError("Unexpected character: '" + character + "'");
      }
    }

    return null;
  }

  private Token readOperator() {
    char character = getChar();

    String op1 = Character.toString(character);
    String op2 = op1 + peekChar();

    if (OPERATOR_CHARACTERS.containsKey(op2)) {
      getChar();
      return new Token(OPERATOR_CHARACTERS.get(op2), op2);
    } else {
      return new Token(OPERATOR_CHARACTERS.get(op1), op1);
    }
  }

  private Token readSyntaxToken() {
    char character = getChar();
    return new Token(SYNTAX_CHARACTERS.get(character), Character.toString(character));
  }

  private Token readProperty() {
    StringBuilder value = new StringBuilder();
    value.append(getChar());

    char character = peekChar();
    if (character != '(') {
      throw new ConditionLexerError(
          String.format(
              "Invalid property reference. Expected '$' to be followed by '(', got '%c'",
              character));
    }

    int nestingLevel = 0;

    while ((character = getChar()) != END_OF_INPUT) {
      value.append(character);

      if (character == '(') {
        ++nestingLevel;
      } else if (character == ')') {
        --nestingLevel;
      }

      if (nestingLevel == 0) {
        break;
      }
    }

    if (nestingLevel > 0) {
      throw new ConditionLexerError(
          "Invalid property reference. Expected a closing ')', got end of input.");
    }

    return new Token(TokenType.PROPERTY, value.toString());
  }

  private Token readQuotedString() {
    StringBuilder value = new StringBuilder();
    boolean expandable = false;
    boolean foundEndQuote = false;

    char character;
    ++position;

    while ((character = getChar()) != END_OF_INPUT) {
      if (character == '\'') {
        foundEndQuote = true;
        break;
      }

      if (character == '$' && peekChar() == '(') {
        expandable = true;
      }

      value.append(character);
    }

    if (!foundEndQuote) {
      throw new ConditionLexerError(
          "Invalid quoted string. Expected a closing quote, got end of input.");
    }

    return new Token(TokenType.STRING, value.toString(), expandable);
  }

  private Token readNumeric() {
    if (data.length() > 2
        && data.charAt(position) == '0'
        && Character.toLowerCase(data.charAt(position + 1)) == 'x') {
      return readHexNumeric();
    } else {
      return readRegularNumeric();
    }
  }

  private Token readHexNumeric() {
    StringBuilder value = new StringBuilder();
    value.append(getChar());
    value.append(getChar());
    while (isHexDigit(peekChar())) {
      value.append(getChar());
    }
    return new Token(TokenType.NUMERIC, value.toString());
  }

  private Token readRegularNumeric() {
    StringBuilder value = new StringBuilder();
    char start = peekChar();
    if (start == '+' || start == '-') {
      value.append(getChar());
    }
    while (true) {
      while (Character.isDigit(peekChar())) {
        value.append(getChar());
      }
      if (peekChar() == '.') {
        value.append(getChar());
      } else {
        break;
      }
    }
    return new Token(TokenType.NUMERIC, value.toString());
  }

  private Token readSimpleStringOrFunction() {
    char character;
    StringBuilder value = new StringBuilder();

    while ((character = peekChar()) != END_OF_INPUT) {
      if (isSimpleStringChar(character)) {
        value.append(getChar());
      } else {
        break;
      }
    }

    String text = value.toString();
    if (text.equalsIgnoreCase("and")) {
      return new Token(TokenType.AND, text);
    } else if (text.equalsIgnoreCase("or")) {
      return new Token(TokenType.OR, text);
    } else {
      skipWhitespace();
      TokenType tokenType = (peekChar() == '(') ? TokenType.FUNCTION : TokenType.STRING;
      return new Token(tokenType, text);
    }
  }

  private static boolean isNumericStart(char character) {
    return character == '+' || character == '-' || character == '.' || Character.isDigit(character);
  }

  private static boolean isHexAlpha(char character) {
    return (character >= 'a' && character <= 'f') || (character >= 'A' && character <= 'F');
  }

  private static boolean isHexDigit(char character) {
    return Character.isDigit(character) || isHexAlpha(character);
  }

  private static boolean isSimpleStringStart(char character) {
    return Character.isAlphabetic(character) || character == '_';
  }

  private static boolean isSimpleStringChar(char character) {
    return isSimpleStringStart(character) || Character.isDigit(character);
  }
}
