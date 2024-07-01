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
    return peekChar(0);
  }

  private char peekChar(int offset) {
    if (position + offset < data.length()) {
      return data.charAt(position + offset);
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

    if (character == END_OF_INPUT) {
      return null;
    } else if (numberReader.isNumberStart(character)) {
      return readNumber();
    } else if (Character.isLetter(character) || character == '_') {
      return readIdentifier();
    } else if (character == '\'') {
      return readString();
    } else if (character == '/' && peekChar(1) == '/') {
      return readLineComment();
    } else if (character == '{' || (character == '(' && peekChar(1) == '*')) {
      return readMultilineComment();
    } else if (OPERATOR_CHARACTERS.containsKey(character)) {
      return readOperator();
    } else if (SYNTAX_CHARACTERS.containsKey(character)) {
      return readSyntaxToken();
    } else {
      throw new ExpressionLexerError("Unexpected character: '" + character + "'");
    }
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

  private Token readString() {
    Token result = readMultilineString();
    if (result == null) {
      result = readSingleLineString();
    }
    return result;
  }

  private Token readSingleLineString() {
    StringBuilder value = new StringBuilder();
    value.append(getChar());

    char character;

    while ((character = getChar()) != END_OF_INPUT && !isNewLine(character)) {
      value.append(character);
      if (character == '\'') {
        if (peekChar() == '\'') {
          value.append(getChar());
        } else {
          break;
        }
      }
    }

    return new Token(TokenType.STRING, value.toString());
  }

  private Token readMultilineString() {
    int lookahead = lookaheadMultilineString(0);
    if (lookahead == 0) {
      return null;
    }

    String value = data.substring(position, position + lookahead);
    position += lookahead;

    return new Token(TokenType.MULTILINE_STRING, value);
  }

  private int lookaheadString(int i) {
    int offset = lookaheadMultilineString(i);
    if (offset == 0) {
      offset = lookaheadSingleLineString(i);
    }
    return i + offset;
  }

  private int lookaheadMultilineString(int i) {
    int startQuotes = lookaheadSingleQuotes(i);
    if (startQuotes >= 3 && (startQuotes % 2 == 1) && isNewLine(peekChar(i + startQuotes))) {
      int offset = startQuotes - 1;
      while (true) {
        switch (peekChar(i + ++offset)) {
          case '\'':
            int quotes = Math.min(startQuotes, lookaheadSingleQuotes(i + offset));
            offset += quotes;
            if (quotes == startQuotes) {
              return offset;
            }
            break;

          case END_OF_INPUT:
            return 0;

          default:
            // do nothing
        }
      }
    }
    return 0;
  }

  private int lookaheadSingleQuotes(int i) {
    int result = 0;
    while (peekChar(i++) == '\'') {
      ++result;
    }
    return result;
  }

  private int lookaheadSingleLineString(int i) {
    int offset = 1;

    char character;

    while ((character = peekChar(i + offset)) != END_OF_INPUT && !isNewLine(character)) {
      ++offset;
      if (character == '\'') {
        if (peekChar(i + offset) == '\'') {
          ++offset;
        } else {
          break;
        }
      }
    }

    return offset;
  }

  private Token readLineComment() {
    StringBuilder value = new StringBuilder();
    char character;

    while ((character = peekChar()) != END_OF_INPUT) {
      if (isNewLine(character)) {
        break;
      }
      value.append(character);
      ++position;
    }

    return new Token(TokenType.COMMENT, value.toString());
  }

  private Token readMultilineComment() {
    int offset = 1;
    Token.TokenType type = TokenType.COMMENT;
    String end;

    if (peekChar() == '(') {
      ++offset;
      end = "*)";
    } else {
      end = "}";
    }

    if (peekChar(offset) == '$') {
      type = TokenType.DIRECTIVE;
    }

    int lookahead = lookaheadMultilineComment(end, offset);
    if (lookahead == 0) {
      return null;
    }

    String value = data.substring(position, position + lookahead);
    position += lookahead;

    return new Token(type, value);
  }

  private int lookaheadLineComment(int i) {
    while (true) {
      int character = peekChar(i);
      if (isNewLine(character) || character == END_OF_INPUT) {
        return i;
      }
      ++i;
    }
  }

  private int lookaheadMultilineComment(String end, int i) {
    char endStart = end.charAt(0);
    String directiveName = null;

    if (peekChar(i) == '$') {
      StringBuilder directiveNameBuilder = new StringBuilder();
      int character = peekChar(i + 1);

      while ((character >= 'a' && character <= 'z')
          || (character >= 'A' && character <= 'Z')
          || Character.isDigit(character)
          || character == '_') {
        ++i;
        directiveNameBuilder.append((char) character);
        character = peekChar(i + 1);
      }

      directiveName = directiveNameBuilder.toString();
    }

    boolean nestedExpression =
        "if".equalsIgnoreCase(directiveName) || "elseif".equalsIgnoreCase(directiveName);

    while (true) {
      int character = peekChar(i);

      if (character == endStart) {
        int j;
        for (j = 1; j < end.length(); ++j) {
          if (peekChar(i + j) != end.charAt(j)) {
            break;
          }
        }
        if (j == end.length()) {
          return i + j;
        }
      }

      switch (character) {
        case '\'':
          if (nestedExpression) {
            i = lookaheadString(i) - 1;
          }
          break;

        case '/':
          if (nestedExpression && peekChar(i + 1) == '/') {
            i = lookaheadLineComment(i + 2);
          }
          break;

        case '{':
          if (nestedExpression) {
            i = lookaheadMultilineComment("}", i + 1);
          }
          break;

        case '(':
          if (nestedExpression && peekChar(i + 1) == '*') {
            i = lookaheadMultilineComment("*)", i + 2);
          }
          break;

        case END_OF_INPUT:
          return 0;

        default:
          // do nothing
      }

      ++i;
    }
  }

  private boolean isNewLine(int c) {
    return c == '\r' || c == '\n';
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
