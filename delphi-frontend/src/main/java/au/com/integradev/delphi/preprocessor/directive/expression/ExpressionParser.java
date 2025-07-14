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

import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.AND;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.COMMENT;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.DIRECTIVE;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.DIV;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.DIVIDE;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.EQUALS;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.GREATER_THAN;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.GREATER_THAN_EQUAL;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.IN;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.LESS_THAN;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.LESS_THAN_EQUAL;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.MINUS;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.MOD;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.MULTIPLY;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.NOT;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.NOT_EQUALS;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.OR;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.PLUS;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.SHL;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.SHR;
import static au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType.XOR;
import static java.util.Objects.requireNonNullElse;

import au.com.integradev.delphi.preprocessor.TextBlockLineEndingMode;
import au.com.integradev.delphi.preprocessor.directive.expression.Token.TokenType;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.lang3.Strings;

public class ExpressionParser {
  public static class ExpressionParserError extends RuntimeException {
    ExpressionParserError(String message, @Nullable Token got) {
      super(message + " Got '" + requireNonNullElse(got, END_OF_INPUT).getText() + "'");
    }
  }

  private static final Token END_OF_INPUT = new Token(TokenType.UNKNOWN, "<end of input>");

  private static final ImmutableSet<TokenType> RELATIONAL_OPERATORS =
      Sets.immutableEnumSet(
          EQUALS, GREATER_THAN, LESS_THAN, GREATER_THAN_EQUAL, LESS_THAN_EQUAL, NOT_EQUALS, IN);

  private static final ImmutableSet<TokenType> ADD_OPERATORS =
      Sets.immutableEnumSet(PLUS, MINUS, OR, XOR);

  private static final ImmutableSet<TokenType> MULTIPLICATION_OPERATORS =
      Sets.immutableEnumSet(MULTIPLY, DIVIDE, DIV, MOD, AND, SHL, SHR);

  private static final ImmutableSet<TokenType> UNARY_OPERATORS =
      Sets.immutableEnumSet(PLUS, MINUS, NOT);

  private final TextBlockLineEndingMode textBlockLineEndingMode;

  // Parser state
  private List<Token> tokens;
  private int position;

  public ExpressionParser(TextBlockLineEndingMode textBlockLineEndingMode) {
    this.textBlockLineEndingMode = textBlockLineEndingMode;
  }

  public Expression parse(List<Token> tokens) {
    this.tokens = tokens;
    this.position = 0;

    return parseExpression();
  }

  private Token peekToken() {
    while (position < tokens.size()) {
      if (tokens.get(position).getType() == COMMENT) {
        ++position;
        continue;
      }
      return tokens.get(position);
    }
    return END_OF_INPUT;
  }

  private Token getToken() {
    Token token = peekToken();
    if (token != END_OF_INPUT) {
      ++position;
    }
    return token;
  }

  private Expression parseExpression() {
    return parseRelational();
  }

  private Expression parseRelational() {
    Expression result = parseAddition();
    Token token;

    while ((token = peekToken()) != END_OF_INPUT) {
      TokenType type = token.getType();
      if (!RELATIONAL_OPERATORS.contains(type)) {
        break;
      }

      getToken();
      result = Expressions.binary(result, type, parseAddition());
    }

    return result;
  }

  private Expression parseAddition() {
    Expression result = parseMultiplication();
    Token token;

    while ((token = peekToken()) != END_OF_INPUT) {
      TokenType type = token.getType();
      if (!ADD_OPERATORS.contains(type)) {
        break;
      }

      getToken();
      result = Expressions.binary(result, type, parseMultiplication());
    }

    return result;
  }

  private Expression parseMultiplication() {
    Expression result = parseUnary();
    Token token;

    while ((token = peekToken()) != END_OF_INPUT) {
      TokenType type = token.getType();
      if (!MULTIPLICATION_OPERATORS.contains(type)) {
        break;
      }

      getToken();
      result = Expressions.binary(result, type, parseUnary());
    }

    return result;
  }

  private Expression parseUnary() {
    Token token = peekToken();

    if (token != END_OF_INPUT) {
      TokenType type = token.getType();
      if (UNARY_OPERATORS.contains(type)) {
        getToken();
        return Expressions.unary(type, parseUnary());
      }
    }

    return parsePrimary();
  }

  private Expression parsePrimary() {
    Token token = peekToken();
    switch (token.getType()) {
      case STRING:
      case MULTILINE_STRING:
      case INTEGER:
      case REAL:
        return parseLiteral();

      case IDENTIFIER:
        return parseIdentifier();

      case LBRACKET:
        return parseSet();

      case LPAREN:
        return parseSubExpression();

      default:
        // Do nothing
    }

    if (token.getType() == DIRECTIVE) {
      // HACK:
      // We don't handle nested directives. When we find one, we just dumbly return a False value.
      //
      // Problem:
      // - We separate parsing into 3 steps:
      //   - Lexing
      //   - Preprocessing
      //   - Parsing
      // - As a result we have 2 sets of lexers/parsers, an ANTLR-generated
      //   `DelphiLexer`/`DelphiParser` and a hand-rolled `CompilerDirectiveParser` with
      //   corresponding `ExpressionLexer`/`ExpressionParser`. The compiler directive parser is used
      //   as part of the preprocessing step to help generate a correct list of tokens for parsing.
      // - The preprocessor is tailored towards DelphiTokens and not the constant expression
      //   tokens generated by `ExpressionLexer`. Code-share would be quite complicated and require
      //   a lot of effort, and the result still wouldn't be fully accurate.
      //
      // To handle this accurately, we need to:
      // - Merge the 3 parsing steps into a hand-rolled "DelphiScanner":
      //   - Lex tokens on-demand instead of doing it upfront.
      //   - Evaluate compiler directives as they're lexed from the input data instead of afterward.
      //   - Perform symbol table construction simultaneously instead of afterward.
      // - When evaluating compiler directives, parse directive expressions using the normal
      //   Delphi expression parser. That will give us nested directive handling for free!
      //
      // For more information, see:
      //   https://github.com/integrated-application-development/sonar-delphi/issues/261
      //
      return Expressions.nameReference("False");
    }

    throw new ExpressionParserError("Expected expression.", token);
  }

  private Expression parseLiteral() {
    Token token = getToken();

    String text;
    switch (token.getType()) {
      case STRING:
        text = evaluateString(token.getText());
        break;
      case MULTILINE_STRING:
        text = evaluateMultilineString(token.getText(), textBlockLineEndingMode);
        break;
      default:
        text = token.getText();
    }

    return Expressions.literal(token.getType(), text);
  }

  private static String evaluateString(String text) {
    text = text.substring(1, text.length() - 1);
    text = text.replace("''", "'");
    return text;
  }

  private String evaluateMultilineString(String text, TextBlockLineEndingMode lineEndingMode) {
    Deque<String> lines = text.lines().collect(Collectors.toCollection(ArrayDeque<String>::new));

    lines.removeFirst();

    String last = lines.removeLast();
    String indentation = readLeadingWhitespace(last);

    String lineEnding;
    switch (lineEndingMode) {
      case CR:
        lineEnding = "\r";
        break;
      case LF:
        lineEnding = "\n";
        break;
      default:
        lineEnding = "\r\n";
    }

    return lines.stream()
        .map(line -> Strings.CS.removeStart(line, indentation))
        .collect(Collectors.joining(lineEnding));
  }

  private static String readLeadingWhitespace(String input) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < input.length(); ++i) {
      char c = input.charAt(i);
      if (c <= 0x20 || c == 0x3000) {
        result.append(c);
      } else {
        break;
      }
    }
    return result.toString();
  }

  private Expression parseIdentifier() {
    StringBuilder identifier = new StringBuilder();
    while (peekToken().getType() == TokenType.IDENTIFIER) {
      identifier.append(getToken().getText());

      Token peek = peekToken();
      if (peek.getType() != TokenType.DOT) {
        if (peek.getType() == TokenType.LPAREN) {
          String invocationName = identifier.toString();
          List<Expression> argumentList = parseArgumentList();
          return Expressions.invocation(invocationName, argumentList);
        }
        break;
      }

      identifier.append(getToken().getText());
    }
    return Expressions.nameReference(identifier.toString());
  }

  private List<Expression> parseArgumentList() {
    List<Expression> arguments = new ArrayList<>();
    getToken();

    Token peek = peekToken();
    if (peek.getType() == TokenType.RPAREN) {
      getToken();
      return arguments;
    }

    while (true) {
      arguments.add(parseExpression());
      Token token = getToken();
      if (token.getType() != TokenType.COMMA) {
        TokenType type = token.getType();
        if (type == TokenType.RPAREN) {
          return arguments;
        }
        throw new ExpressionParserError("Expected ',' or ')' in argument list.", token);
      }
    }
  }

  private Expression parseSet() {
    getToken();

    Token peek = peekToken();
    if (peek.getType() == TokenType.RBRACKET) {
      getToken();
      return Expressions.emptySet();
    }

    Set<Expression> elements = new HashSet<>();

    while (true) {
      elements.add(parseExpression());
      Token token = getToken();
      if (token.getType() != TokenType.COMMA) {
        TokenType type = token.getType();
        if (type == TokenType.RBRACKET) {
          return Expressions.set(elements);
        }
        throw new ExpressionParserError("Expected ',' or ']' in set literal.", token);
      }
    }
  }

  private Expression parseSubExpression() {
    getToken();

    Expression result = parseExpression();
    Token token = getToken();

    if (token.getType() == TokenType.RPAREN) {
      return result;
    }

    throw new ExpressionParserError("Expected ')' after expression.", token);
  }
}
