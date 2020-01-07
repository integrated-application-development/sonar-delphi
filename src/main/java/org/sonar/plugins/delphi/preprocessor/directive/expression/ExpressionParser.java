package org.sonar.plugins.delphi.preprocessor.directive.expression;

import static com.google.common.collect.Sets.immutableEnumSet;
import static java.util.Objects.requireNonNullElse;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.AND;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.DIV;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.DIVIDE;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.EQUALS;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.GREATER_THAN;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.GREATER_THAN_EQUAL;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.IN;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.LESS_THAN;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.LESS_THAN_EQUAL;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.MINUS;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.MOD;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.MULTIPLY;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.NOT;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.NOT_EQUALS;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.OR;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.PLUS;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.SHL;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.SHR;
import static org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType.XOR;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.plugins.delphi.preprocessor.directive.CompilerDirective.Expression;
import org.sonar.plugins.delphi.preprocessor.directive.expression.Token.TokenType;

public class ExpressionParser {
  public static class ExpressionParserError extends RuntimeException {
    ExpressionParserError(String message, @Nullable Token got) {
      super(message + " Got '" + requireNonNullElse(got, END_OF_INPUT).getText() + "'");
    }
  }

  private static final Token END_OF_INPUT = new Token(TokenType.UNKNOWN, "<end of input>");

  private static final ImmutableSet<TokenType> RELATIONAL_OPERATORS =
      immutableEnumSet(
          EQUALS, GREATER_THAN, LESS_THAN, GREATER_THAN_EQUAL, LESS_THAN_EQUAL, NOT_EQUALS, IN);

  private static final ImmutableSet<TokenType> ADD_OPERATORS =
      immutableEnumSet(PLUS, MINUS, OR, XOR);

  private static final ImmutableSet<TokenType> MULTIPLICATION_OPERATORS =
      immutableEnumSet(MULTIPLY, DIVIDE, DIV, MOD, AND, SHL, SHR);

  private static final ImmutableSet<TokenType> UNARY_OPERATORS = immutableEnumSet(PLUS, MINUS, NOT);

  // Parser state
  private List<Token> tokens;
  private int position;

  public Expression parse(List<Token> tokens) {
    this.tokens = tokens;
    this.position = 0;

    return parseExpression();
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
      case INTEGER:
      case DECIMAL:
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

    throw new ExpressionParserError("Expected expression.", token);
  }

  private Expression parseLiteral() {
    Token token = getToken();
    return Expressions.literal(token.getType(), token.getText());
  }

  private Expression parseIdentifier() {
    Token token;
    StringBuilder identifier = new StringBuilder();
    while ((token = peekToken()) != END_OF_INPUT) {
      if (token.getType() == TokenType.IDENTIFIER) {
        identifier.append(getToken().getText());
      }

      Token peek = peekToken();

      if (peek.getType() != TokenType.DOT) {
        if (peek.getType() == TokenType.LPAREN) {
          String invocationName = identifier.toString();
          List<Expression> argumentList = parseArgumentList();
          return Expressions.invocation(invocationName, argumentList);
        }
        break;
      }

      getToken();
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
