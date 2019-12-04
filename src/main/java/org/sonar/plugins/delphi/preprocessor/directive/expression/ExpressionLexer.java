package org.sonar.plugins.delphi.preprocessor.directive.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nullable;
import org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType;

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
      if (Character.isDigit(character)) {
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
    TokenType type = TokenType.INTEGER;
    char character;
    StringBuilder value = new StringBuilder();

    while ((character = peekChar()) != END_OF_INPUT) {
      if (character == '.') {
        if (type == TokenType.DECIMAL) {
          throw new ExpressionLexerError("Unexpected '.' in numeric literal");
        }
        type = TokenType.DECIMAL;
      } else if (!Character.isDigit(character)) {
        break;
      }
      value.append(getChar());
    }

    return new Token(type, value.toString());
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
    return new Token(SYNTAX_CHARACTERS.get(character), Character.toString(character));
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
}
