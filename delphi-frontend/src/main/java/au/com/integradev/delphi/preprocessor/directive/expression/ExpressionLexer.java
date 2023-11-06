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

import au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import javax.annotation.Nullable;

public class ExpressionLexer {
  public static class ExpressionLexerError extends RuntimeException {
    ExpressionLexerError(String message) {
      super(message);
    }
  }

  private static final Map<Character, TokenType> SYNTAX_CHARACTERS =
      Map.of(
          '.', TokenType.DOT,
          ',', TokenType.COMMA,
          '(', TokenType.LPAREN,
          ')', TokenType.RPAREN,
          '[', TokenType.LBRACKET,
          ']', TokenType.RBRACKET);

  private static final Map<Character, TokenType> OPERATOR_CHARACTERS =
      Map.of(
          '=', TokenType.EQUALS,
          '<', TokenType.LESS_THAN,
          '>', TokenType.GREATER_THAN,
          '+', TokenType.PLUS,
          '-', TokenType.MINUS,
          '*', TokenType.MULTIPLY,
          '/', TokenType.DIVIDE);

  private static final Map<String, TokenType> OPERATOR_IDENTIFIERS =
      new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  static {
    OPERATOR_IDENTIFIERS.put("div", TokenType.DIV);
    OPERATOR_IDENTIFIERS.put("mod", TokenType.MOD);
    OPERATOR_IDENTIFIERS.put("shl", TokenType.SHL);
    OPERATOR_IDENTIFIERS.put("shr", TokenType.SHR);
    OPERATOR_IDENTIFIERS.put("in", TokenType.IN);
    OPERATOR_IDENTIFIERS.put("not", TokenType.NOT);
    OPERATOR_IDENTIFIERS.put("and", TokenType.AND);
    OPERATOR_IDENTIFIERS.put("or", TokenType.OR);
    OPERATOR_IDENTIFIERS.put("xor", TokenType.XOR);
  }

  private static final char END_OF_INPUT = '\0';
  private String data;
  private int position;
  private final NumberReader numberReader = new NumberReader();

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

  @Nullable
  private Token readToken() {
    char character;

    // Trim out whitespace
    while ((character = peekChar()) != END_OF_INPUT) {
      if (!Character.isWhitespace(character)) {
        break;
      }
      ++position;
    }

    if (character != END_OF_INPUT) {
      if (numberReader.isNumberStart(character)) {
        return readNumber();
      } else if (Character.isLetter(character) || character == '_') {
        return readIdentifier();
      } else if (OPERATOR_CHARACTERS.containsKey(character)) {
        return readOperator();
      } else if (SYNTAX_CHARACTERS.containsKey(character)) {
        return readSyntaxToken();
      } else if (character == '\'') {
        return readSingleQuoteString();
      } else {
        throw new ExpressionLexerError("Unexpected character: '" + character + "'");
      }
    }

    return null;
  }

  private Token readNumber() {
    return numberReader.read();
  }

  private Token readOperator() {
    char character = getChar();
    String characterString = Character.toString(character);

    if (character == '<' && peekChar() == '>') {
      return new Token(TokenType.NOT_EQUALS, characterString + getChar());
    } else if (character == '<' && peekChar() == '=') {
      return new Token(TokenType.LESS_THAN_EQUAL, characterString + getChar());
    } else if (character == '>' && peekChar() == '=') {
      return new Token(TokenType.GREATER_THAN_EQUAL, characterString + getChar());
    } else {
      return new Token(OPERATOR_CHARACTERS.get(character), characterString);
    }
  }

  private Token readSyntaxToken() {
    char character = getChar();
    String characterString = Character.toString(character);

    if (character == '(' && peekChar() == '.') {
      return new Token(TokenType.LBRACKET, characterString + getChar());
    } else if (character == '.' && peekChar() == ')') {
      return new Token(TokenType.RBRACKET, characterString + getChar());
    } else {
      return new Token(SYNTAX_CHARACTERS.get(character), Character.toString(character));
    }
  }

  private Token readIdentifier() {
    char character;
    StringBuilder identifier = new StringBuilder();

    while ((character = peekChar()) != END_OF_INPUT) {
      if (character == '_' || Character.isLetter(character) || Character.isDigit(character)) {
        identifier.append(getChar());
      } else {
        break;
      }
    }

    String text = identifier.toString();
    TokenType type = OPERATOR_IDENTIFIERS.getOrDefault(text, TokenType.IDENTIFIER);

    return new Token(type, text);
  }

  private Token readSingleQuoteString() {
    getChar();
    StringBuilder value = new StringBuilder();
    char character;

    while ((character = getChar()) != END_OF_INPUT) {
      if (character == '\'') {
        if (peekChar() != '\'') {
          break;
        }
        getChar();
      } else {
        value.append(character);
      }
    }

    return new Token(TokenType.STRING, value.toString());
  }

  private static boolean isHexDigit(char character) {
    character = Character.toLowerCase(character);
    return Character.isDigit(character) || (character >= 'a' && character <= 'f');
  }

  private static boolean isBinaryDigit(char character) {
    return character == '0' || character == '1';
  }

  private final class NumberReader {
    TokenType type;
    StringBuilder value = new StringBuilder();
    private Predicate<Character> isDigitCharacter;
    private boolean canBeReal;

    private void init() {
      type = TokenType.INTEGER;
      value.setLength(0);
      switch (peekChar()) {
        case '$':
          isDigitCharacter = ExpressionLexer::isHexDigit;
          canBeReal = false;
          value.append(getChar());
          break;
        case '%':
          isDigitCharacter = ExpressionLexer::isBinaryDigit;
          canBeReal = false;
          value.append(getChar());
          break;
        default:
          isDigitCharacter = Character::isDigit;
          canBeReal = true;
      }
    }

    private boolean readCharacter(char character) {
      if (character == '.') {
        if (type == TokenType.REAL || !canBeReal) {
          throw new ExpressionLexerError("Unexpected '.' in numeric literal");
        }
        type = TokenType.REAL;
        value.append(getChar());
        return true;
      }

      if (canBeReal && Character.toLowerCase(character) == 'e') {
        type = TokenType.REAL;
        value.append(getChar());
        if (peekChar() == '+' || peekChar() == '-') {
          value.append(getChar());
        }
        if (!readDigitSequence(Character::isDigit)) {
          throw new ExpressionLexerError("Expected a digit sequence to follow E");
        }
        return false;
      }

      return readDigitSequence(isDigitCharacter);
    }

    private boolean readDigitSequence(Predicate<Character> isDigitCharacter) {
      boolean result = false;
      char character;
      while ((character = peekChar()) != END_OF_INPUT) {
        if (isDigitCharacter.test(character) || (value.length() > 0 && character == '_')) {
          value.append(getChar());
          result = true;
        } else {
          break;
        }
      }
      return result;
    }

    public Token read() {
      init();
      char character;
      while ((character = peekChar()) != END_OF_INPUT) {
        if (!readCharacter(character)) {
          break;
        }
      }
      return new Token(type, value.toString());
    }

    public boolean isNumberStart(char character) {
      return Character.isDigit(character) || character == '$' || character == '%';
    }
  }
}
