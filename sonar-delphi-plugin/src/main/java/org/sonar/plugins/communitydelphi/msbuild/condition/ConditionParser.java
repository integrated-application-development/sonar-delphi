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
package org.sonar.plugins.communitydelphi.msbuild.condition;

import static java.util.Objects.requireNonNullElse;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.msbuild.condition.Token.TokenType;

public class ConditionParser {
  public static class ConditionParserError extends RuntimeException {
    ConditionParserError(String message, @Nullable Token got) {
      super(message + " Got '" + requireNonNullElse(got, END_OF_INPUT).getText() + "'");
    }
  }

  private static final Set<TokenType> RELATIONAL_OPERATORS =
      Sets.immutableEnumSet(
          TokenType.LESS_THAN,
          TokenType.GREATER_THAN,
          TokenType.LESS_THAN_EQUAL,
          TokenType.GREATER_THAN_EQUAL,
          TokenType.EQUAL,
          TokenType.NOT_EQUAL);

  private static final Token END_OF_INPUT = new Token(TokenType.END_OF_INPUT, "<end of input>");

  private List<Token> tokens;
  private int position;

  public Expression parse(List<Token> tokens) {
    this.tokens = tokens;
    this.position = 0;

    Expression expression = parseCondition();

    Token token = getToken();
    if (token != END_OF_INPUT) {
      throw new ConditionParserError(
          "Unexpected token in condition. Expected " + END_OF_INPUT.getText(), token);
    }

    return expression;
  }

  private Token peekToken() {
    if (position < tokens.size()) {
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

  private Expression parseCondition() {
    Expression expression = parseBooleanTerm();
    if (peekToken() != END_OF_INPUT) {
      expression = parseOr(expression);
    }
    return expression;
  }

  private Expression parseOr(Expression left) {
    if (peekToken().getType() == TokenType.OR) {
      ++position;
      Expression right = parseBooleanTerm();
      Expression expression = new BinaryExpression(left, TokenType.OR, right);
      return parseOr(expression);
    }
    return left;
  }

  private Expression parseBooleanTerm() {
    Expression expression = parseRelational();
    if (peekToken() != END_OF_INPUT) {
      expression = parseAnd(expression);
    }
    return expression;
  }

  private Expression parseAnd(Expression left) {
    if (peekToken().getType() == TokenType.AND) {
      ++position;
      Expression right = parseRelational();
      Expression expression = new BinaryExpression(left, TokenType.AND, right);
      return parseAnd(expression);
    }
    return left;
  }

  private Expression parseRelational() {
    Expression left = parseFactor();

    if (!RELATIONAL_OPERATORS.contains(peekToken().getType())) {
      return left;
    }

    TokenType operator = getToken().getType();
    Expression right = parseFactor();

    return new BinaryExpression(left, operator, right);
  }

  private Expression parseFactor() {
    Expression arg = this.parseArg();
    if (arg != null) {
      return arg;
    }

    switch (peekToken().getType()) {
      case FUNCTION:
        return parseFunction();
      case LPAREN:
        return parseParenthesized();
      case NOT:
        return parseNot();
      default:
        throw new ConditionParserError("Unexpected token in condition.", peekToken());
    }
  }

  private Expression parseFunction() {
    String name = getToken().getText();

    if (peekToken().getType() != TokenType.LPAREN) {
      throw new ConditionParserError("Expected opening parentheses '('.", peekToken());
    }
    ++position;

    List<Expression> arguments = parseArgList();
    if (peekToken().getType() != TokenType.RPAREN) {
      throw new ConditionParserError("Expected closing parentheses ')'.", peekToken());
    }
    ++position;

    return new FunctionCallExpression(name, arguments);
  }

  private Expression parseParenthesized() {
    ++position;
    Expression child = parseCondition();
    if (peekToken().getType() != TokenType.RPAREN) {
      throw new ConditionParserError("Expected closing parentheses ')'.", peekToken());
    }
    ++position;
    return child;
  }

  private Expression parseNot() {
    ++position;
    Expression expression = parseFactor();
    return new NotExpression(expression);
  }

  private List<Expression> parseArgList() {
    if (peekToken().getType() == TokenType.RPAREN) {
      return Collections.emptyList();
    }

    List<Expression> args = new ArrayList<>();
    while (true) {
      args.add(this.parseArg());
      if (peekToken().getType() != TokenType.COMMA) {
        break;
      }
      getToken();
    }
    return List.copyOf(args);
  }

  private Expression parseArg() {
    switch (peekToken().getType()) {
      case STRING:
        Token token = getToken();
        return new StringExpression(token.getText(), token.getExpandable());
      case NUMERIC:
        return new NumericExpression(getToken().getText());
      case PROPERTY:
        return new StringExpression(getToken().getText(), true);
      default:
        return null;
    }
  }
}
