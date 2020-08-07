package org.sonar.plugins.delphi.preprocessor.directive;

import static org.sonar.plugins.delphi.preprocessor.directive.CompilerDirectiveParser.DirectiveBracketType.CURLY;
import static org.sonar.plugins.delphi.preprocessor.directive.CompilerDirectiveParser.DirectiveBracketType.PAREN;
import static org.sonar.plugins.delphi.preprocessor.directive.CompilerDirectiveType.IFDEF;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionLexer;
import org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionLexer.ExpressionLexerError;
import org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionParser;
import org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionParser.ExpressionParserError;

/**
 * Parses a CompilerDirective object from a TkCompilerDirective token.
 *
 * <p>Example: A token with the text "{$include unit.pas}" will create a CompilerDirective with type
 * INCLUDE.
 */
class CompilerDirectiveParser {
  static class CompilerDirectiveParserError extends RuntimeException {
    private CompilerDirectiveParserError(Exception e, Token token) {
      super(e.getMessage() + " <Line " + token.getLine() + ">", e);
    }
  }

  private static final ExpressionLexer EXPRESSION_LEXER = new ExpressionLexer();
  private static final ExpressionParser EXPRESSION_PARSER = new ExpressionParser();

  private static final char END_OF_INPUT = '\0';

  enum DirectiveBracketType {
    CURLY,
    PAREN
  }

  // Parser state
  private String data;
  private int position;

  // Current directive state
  private Token token;
  private DirectiveBracketType directiveBracketType;
  private CompilerDirectiveType type;
  private final StringBuilder directiveName = new StringBuilder();
  private final StringBuilder directiveItem = new StringBuilder();
  private final StringBuilder trimmedDirectiveItem = new StringBuilder();

  /**
   * Produce a list of compiler directives from a string
   *
   * @param token Token to parse into a CompilerDirective object
   * @return Compiler directive
   */
  public CompilerDirective parse(Token token) {
    this.data = token.getText();
    this.position = 0;

    this.token = token;
    this.directiveBracketType = (getChar(position) == '{') ? CURLY : PAREN;

    if (directiveBracketType == CURLY) {
      position += 2;
      // Jump ahead of the "{$"
    }

    if (directiveBracketType == PAREN) {
      position += 3;
      // Jump ahead of the "(*$"
    }

    parseDirectiveName();
    parseDirectiveItem();

    return createDirective();
  }

  private CompilerDirective createDirective() {
    String item = directiveItem.toString();
    String trimmedItem = trimmedDirectiveItem.toString();

    switch (type) {
      case DEFINE:
        return new DefineDirective(token, type, trimmedItem);

      case UNDEFINE:
        return new UndefineDirective(token, type, trimmedItem);

      case IFDEF:
      case IFNDEF:
        BranchDirective ifDefBranch = new IfDefDirective(token, type, trimmedItem, type == IFDEF);
        return new BranchingDirective(ifDefBranch);

      case IFOPT:
        BranchDirective ifOptBranch = new IfOptDirective(token, type);
        return new BranchingDirective(ifOptBranch);

      case IF:
        BranchDirective ifBranch = new IfDirective(token, type, parseExpression(item));
        return new BranchingDirective(ifBranch);

      case ELSEIF:
        return new ElseIfDirective(token, type, parseExpression(item));

      case ELSE:
        return new ElseDirective(token, type);

      case ENDIF:
      case IFEND:
        return new EndIfDirective(token, type);

      case INCLUDE:
        return new IncludeDirective(token, type, trimmedItem);

      case SCOPEDENUMS:
        return new ScopedEnumsDirective(token, type, trimmedItem);

      case POINTERMATH:
        return new PointerMathDirective(token, type, trimmedItem);

      case HINTS:
        return new HintsDirective(token, type, trimmedItem);

      case WARNINGS:
        return new WarningsDirective(token, type, trimmedItem);

      case WARN:
        return new WarnDirective(token, type, item);

      default:
        return new DefaultCompilerDirective(token, type);
    }
  }

  private CompilerDirective.Expression parseExpression(String item) {
    try {
      var tokens = EXPRESSION_LEXER.lex(item);
      return EXPRESSION_PARSER.parse(tokens);
    } catch (ExpressionLexerError | ExpressionParserError e) {
      throw new CompilerDirectiveParserError(e, token);
    }
  }

  private void parseDirectiveName() {
    directiveName.setLength(0);
    char character = getChar(position);

    while (true) {
      if (Character.isWhitespace(character) || isEndOfDirective(character)) {
        String name = directiveName.toString();
        type = CompilerDirectiveType.getTypeByName(name);
        return;
      }

      directiveName.append(character);
      character = getChar(++position);
    }
  }

  private void parseDirectiveItem() {
    directiveItem.setLength(0);
    trimmedDirectiveItem.setLength(0);
    char character = getChar(position);
    boolean insideQuote = false;
    boolean foundWhitespace = false;

    while (Character.isWhitespace(character)) {
      character = getChar(++position);
    }

    while (true) {
      if (character == '\'') {
        insideQuote = !insideQuote;
        character = getChar(++position);
        continue;
      }

      if (!insideQuote && Character.isWhitespace(character)) {
        foundWhitespace = true;
      } else if (isEndOfDirective(character)) {
        return;
      }

      directiveItem.append(character);
      if (!foundWhitespace) {
        trimmedDirectiveItem.append(character);
      }
      character = getChar(++position);
    }
  }

  private boolean isEndOfDirective(char character) {
    boolean result = false;

    if (directiveBracketType == CURLY) {
      result = (character == '}');
    }

    if (directiveBracketType == PAREN) {
      result = (character == '*' && peekChar() == ')');
    }

    return result;
  }

  private char peekChar() {
    return getChar(position + 1);
  }

  private char getChar(int position) {
    return (position < data.length()) ? data.charAt(position) : END_OF_INPUT;
  }
}
