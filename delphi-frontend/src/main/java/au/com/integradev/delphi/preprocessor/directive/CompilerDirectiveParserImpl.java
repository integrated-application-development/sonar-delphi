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
package au.com.integradev.delphi.preprocessor.directive;

import static au.com.integradev.delphi.preprocessor.directive.CompilerDirectiveParserImpl.DirectiveBracketType.CURLY;
import static au.com.integradev.delphi.preprocessor.directive.CompilerDirectiveParserImpl.DirectiveBracketType.PAREN;

import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.preprocessor.PreprocessorException;
import au.com.integradev.delphi.preprocessor.TextBlockLineEndingModeRegistry;
import au.com.integradev.delphi.preprocessor.directive.expression.Expression;
import au.com.integradev.delphi.preprocessor.directive.expression.ExpressionLexer;
import au.com.integradev.delphi.preprocessor.directive.expression.ExpressionLexer.ExpressionLexerError;
import au.com.integradev.delphi.preprocessor.directive.expression.ExpressionParser;
import au.com.integradev.delphi.preprocessor.directive.expression.ExpressionParser.ExpressionParserError;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.sonar.plugins.communitydelphi.api.directive.CompilerDirective;
import org.sonar.plugins.communitydelphi.api.directive.CompilerDirectiveParser;
import org.sonar.plugins.communitydelphi.api.directive.ConditionalDirective.ConditionalKind;
import org.sonar.plugins.communitydelphi.api.directive.ParameterDirective.ParameterKind;
import org.sonar.plugins.communitydelphi.api.directive.ResourceDirective;
import org.sonar.plugins.communitydelphi.api.directive.SwitchDirective.SwitchKind;
import org.sonar.plugins.communitydelphi.api.directive.TextBlockDirective;
import org.sonar.plugins.communitydelphi.api.directive.TextBlockDirective.LineEndingKind;
import org.sonar.plugins.communitydelphi.api.directive.WarnDirective;
import org.sonar.plugins.communitydelphi.api.directive.WarnDirective.WarnParameterValue;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

public class CompilerDirectiveParserImpl implements CompilerDirectiveParser {
  private static final ExpressionLexer EXPRESSION_LEXER = new ExpressionLexer();

  private static final char END_OF_INPUT = '\0';

  enum DirectiveBracketType {
    CURLY,
    PAREN
  }

  private final Platform platform;
  private final TextBlockLineEndingModeRegistry textBlockLineEndingModeRegistry;

  // Parser state
  private String data;
  private int position;

  // Current directive state
  private DelphiToken token;
  private DirectiveBracketType directiveBracketType;

  public CompilerDirectiveParserImpl(
      Platform platform, TextBlockLineEndingModeRegistry textBlockLineEndingModeRegistry) {
    this.platform = platform;
    this.textBlockLineEndingModeRegistry = textBlockLineEndingModeRegistry;
  }

  @Override
  public Optional<CompilerDirective> parse(DelphiToken token) {
    this.data = token.getImage();
    this.position = 0;

    this.token = token;
    this.directiveBracketType = (currentChar() == '{') ? CURLY : PAREN;

    if (directiveBracketType == CURLY) {
      position += 2;
      // Jump ahead of the "{$"
    }

    if (directiveBracketType == PAREN) {
      position += 3;
      // Jump ahead of the "(*$"
    }

    return Optional.ofNullable(createDirective(readName()));
  }

  private CompilerDirective createDirective(String name) {
    Optional<SwitchKind> switchKind = SwitchKind.find(name);
    Optional<Boolean> shortSwitchValue =
        (name.length() == 1) ? readShortSwitchValue() : Optional.empty();

    if (switchKind.isPresent()) {
      Optional<Boolean> switchValue = shortSwitchValue.or(this::readLongSwitchValue);
      if (switchValue.isPresent()) {
        return new SwitchDirectiveImpl(token, switchKind.get(), switchValue.get());
      } else if (isMinEnumSize(switchKind.get())) {
        return new SwitchDirectiveImpl(token, switchKind.get(), true);
      }
    }

    if (shortSwitchValue.isPresent()) {
      // The syntax indicates that this can only be a switch.
      return null;
    }

    Optional<ParameterKind> parameterKind = ParameterKind.find(name, platform);
    if (parameterKind.isPresent()) {
      ParameterKind kind = parameterKind.get();
      switch (kind) {
        case DEFINE:
          return new DefineDirectiveImpl(token, readDirectiveParameter());
        case UNDEF:
          return new UndefineDirectiveImpl(token, readDirectiveParameter());
        case INCLUDE:
          return new IncludeDirectiveImpl(token, readDirectiveParameter());
        case RESOURCE:
          return createResourceDirective();
        case WARN:
          return createWarnDirective();
        case TEXTBLOCK:
          return createTextBlockDirective();
        default:
          return new ParameterDirectiveImpl(token, kind);
      }
    }

    Optional<ConditionalKind> conditionalKind = ConditionalKind.find(name);
    if (conditionalKind.isPresent()) {
      ConditionalKind kind = conditionalKind.get();
      switch (kind) {
        case IFDEF:
          return new IfDefDirectiveImpl(token, readDirectiveParameter());

        case IFNDEF:
          return new IfnDefDirectiveImpl(token, readDirectiveParameter());

        case IFOPT:
          return createIfOptDirective();

        case IF:
          return new IfDirective(token, readExpression());

        case ELSEIF:
          return new ElseIfDirective(token, readExpression());

        case ELSE:
          return new ElseDirective(token);

        case ENDIF:
          return new EndIfDirective(token);

        case IFEND:
          return new IfEndDirective(token);
      }
    }

    return null;
  }

  private static boolean isMinEnumSize(SwitchKind switchKind) {
    return switchKind == SwitchKind.MINENUMSIZE1
        || switchKind == SwitchKind.MINENUMSIZE2
        || switchKind == SwitchKind.MINENUMSIZE4;
  }

  private ResourceDirective createResourceDirective() {
    String resourceFile = readDirectiveParameter();
    String resourceScriptFile = null;
    List<String> predicates = new ArrayList<>();

    char character = currentChar();
    while (Character.isWhitespace(character)) {
      character = nextChar();
    }

    if (character == '\'') {
      resourceScriptFile = StringUtils.stripToNull(readDirectiveParameter());
    } else {
      while (true) {
        String predicate = StringUtils.stripToNull(readDirectiveParameter());
        if (predicate == null) {
          break;
        }
        predicates.add(predicate);
      }
    }

    return new ResourceDirectiveImpl(token, resourceFile, resourceScriptFile, predicates);
  }

  private WarnDirective createWarnDirective() {
    String identifier = readDirectiveParameter();
    String parameter = readDirectiveParameter();
    WarnParameterValue value = EnumUtils.getEnumIgnoreCase(WarnParameterValue.class, parameter);
    if (value == null) {
      return null;
    }
    return new WarnDirectiveImpl(token, identifier, value);
  }

  private TextBlockDirective createTextBlockDirective() {
    String parameter = readDirectiveParameter();
    LineEndingKind lineEndingKind = EnumUtils.getEnumIgnoreCase(LineEndingKind.class, parameter);
    if (lineEndingKind == null) {
      return null;
    }
    return new TextBlockDirectiveImpl(token, lineEndingKind);
  }

  private IfOptDirective createIfOptDirective() {
    char character = currentChar();
    while (Character.isWhitespace(character)) {
      character = nextChar();
    }
    String switchName = readName();
    if (switchName.length() == 1) {
      Optional<SwitchKind> switchKind = SwitchKind.find(switchName);
      if (switchKind.isPresent()) {
        Optional<Boolean> switchValue = readShortSwitchValue();
        if (switchValue.isPresent()) {
          return new IfOptDirective(token, switchKind.get(), switchValue.get());
        }
      }
    }
    return null;
  }

  private String readName() {
    StringBuilder name = new StringBuilder();
    char character = currentChar();

    while ((character >= 'a' && character <= 'z')
        || (character >= 'A' && character <= 'Z')
        || Character.isDigit(character)
        || character == '_') {
      name.append(character);
      character = nextChar();
    }

    return name.toString();
  }

  private Optional<Boolean> readShortSwitchValue() {
    char character = currentChar();
    if (character == '+' || character == '-') {
      nextChar();
      return Optional.of(character == '+');
    }
    return Optional.empty();
  }

  private Optional<Boolean> readLongSwitchValue() {
    int oldPosition = position;
    String item = readDirectiveParameter();
    switch (item.toUpperCase()) {
      case "ON":
        return Optional.of(true);
      case "OFF":
        return Optional.of(false);
      default:
        position = oldPosition;
        return Optional.empty();
    }
  }

  private String readDirectiveParameter() {
    StringBuilder item = new StringBuilder();
    char character = currentChar();
    boolean insideQuote = false;

    while (Character.isWhitespace(character)) {
      character = nextChar();
    }

    while (!(Character.isWhitespace(character) && !insideQuote) && !isEndOfDirective(character)) {
      if (character == '\'') {
        insideQuote = !insideQuote;
        character = nextChar();
        continue;
      }

      item.append(character);
      character = nextChar();
    }

    return item.toString();
  }

  private Expression readExpression() {
    StringBuilder input = new StringBuilder();
    char character = currentChar();

    while (!isEndOfDirective(character)) {
      input.append(character);
      character = nextChar();
    }

    try {
      var tokens = EXPRESSION_LEXER.lex(input.toString());
      return expressionParser().parse(tokens);
    } catch (ExpressionLexerError | ExpressionParserError e) {
      throw new CompilerDirectiveParserError(token, e);
    }
  }

  private ExpressionParser expressionParser() {
    int index = token.getIndex();
    return new ExpressionParser(textBlockLineEndingModeRegistry.getLineEndingMode(index));
  }

  private boolean isEndOfDirective(char character) {
    boolean result = false;

    if (directiveBracketType == CURLY) {
      result = character == '}' && peekChar() == END_OF_INPUT;
    }

    if (directiveBracketType == PAREN) {
      result = character == '*' && peekChar() == ')' && peekChar(1) == END_OF_INPUT;
    }

    return result;
  }

  private char currentChar() {
    return getChar(position);
  }

  private char nextChar() {
    ++position;
    return getChar(position);
  }

  private char peekChar() {
    return peekChar(0);
  }

  private char peekChar(int offset) {
    return getChar(position + offset + 1);
  }

  private char getChar(int position) {
    return (position < data.length()) ? data.charAt(position) : END_OF_INPUT;
  }

  static final class CompilerDirectiveParserError extends PreprocessorException {
    private CompilerDirectiveParserError(DelphiToken token, Throwable cause) {
      super(cause.getMessage(), token, cause);
    }
  }
}
