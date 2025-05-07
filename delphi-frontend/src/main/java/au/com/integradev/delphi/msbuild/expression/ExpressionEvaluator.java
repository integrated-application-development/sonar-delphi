/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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
package au.com.integradev.delphi.msbuild.expression;

import au.com.integradev.delphi.msbuild.MSBuildItem;
import au.com.integradev.delphi.msbuild.MSBuildState;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpressionEvaluator {
  private static final Logger LOG = LoggerFactory.getLogger(ExpressionEvaluator.class);
  private static final Pattern METADATA_EXPRESSION_PATTERN =
      Pattern.compile("%\\(\\s*(\\w+)\\s*\\)");

  private final MSBuildState state;
  private String text;
  private int pos;
  private final UnaryOperator<String> metadataProvider;

  private char nextChar() {
    if (pos >= text.length()) {
      return 0;
    }
    return text.charAt(pos++);
  }

  private char peekChar() {
    if (pos >= text.length()) {
      return 0;
    }
    return text.charAt(pos);
  }

  private boolean eof() {
    return pos == text.length();
  }

  public ExpressionEvaluator(MSBuildState state) {
    this(state, null);
  }

  public ExpressionEvaluator(MSBuildState state, UnaryOperator<String> metadataProvider) {
    this.state = state;
    this.metadataProvider = metadataProvider;
  }

  public String eval(String text) {
    if (text == null || text.isBlank()) {
      return text;
    }

    this.text = text;
    this.pos = 0;
    return parseTopLevel(false, true);
  }

  public MSBuildState getState() {
    return state;
  }

  private Optional<String> tryParse(Supplier<Optional<String>> parseFunc) {
    int savedPos = pos;
    var result = parseFunc.get();
    if (result.isEmpty()) {
      pos = savedPos;
    }
    return result;
  }

  private String parseTopLevel(boolean stopAtQuote, boolean parseMetadata) {
    var result = new StringBuilder();

    while (!eof()) {
      int prevPos = pos;

      // Handle MSBuild expressions starting at this char
      switch (peekChar()) {
        case '$':
          tryParse(this::parseProperty).ifPresent(result::append);
          break;
        case '@':
          tryParse(this::parseItem).ifPresent(result::append);
          break;
        case '%':
          if (parseMetadata) {
            tryParse(this::parseMetadata).ifPresent(result::append);
          }
          break;
        case '\'':
          if (stopAtQuote) {
            return result.toString();
          }
          break;
        default:
      }

      if (prevPos == pos) {
        // Nothing more complex was parseable, simply treat it as normal text
        result.append(nextChar());
      }
    }

    return result.toString();
  }

  private Optional<String> parseItem() {
    if (nextChar() != '@' || nextChar() != '(') {
      return Optional.empty();
    }

    skipWhitespace();
    Optional<String> ident = parseIdentifier();
    if (ident.isEmpty()) {
      return Optional.empty();
    }
    skipWhitespace();

    Function<MSBuildItem, String> transform = MSBuildItem::getIdentity;
    String separator = ";";

    if (peekChar() == '-') {
      var maybeTransform = parseItemTransform();
      if (maybeTransform.isEmpty()) {
        return Optional.empty();
      }
      transform = maybeTransform.get();

      skipWhitespace();

      if (peekChar() == '-') {
        unsupportedFeature("chained item transform");
        return Optional.empty();
      }
    }

    if (peekChar() == ',') {
      nextChar();
      skipWhitespace();
      var maybeSeparator = parseSimpleString();
      if (maybeSeparator.isEmpty()) {
        return Optional.empty();
      }
      separator = maybeSeparator.get();
      skipWhitespace();
    }

    if (nextChar() != ')') {
      return Optional.empty();
    }

    return Optional.of(concatItems(ident.get(), transform, separator));
  }

  private String concatItems(
      String name, Function<MSBuildItem, String> transform, CharSequence separator) {
    return state.getItems(name).stream().map(transform).collect(Collectors.joining(separator));
  }

  private Optional<Function<MSBuildItem, String>> parseItemTransform() {
    if (nextChar() != '-' || nextChar() != '>') {
      return Optional.empty();
    }

    return parseItemTransformExpression();
  }

  private Optional<Function<MSBuildItem, String>> parseItemTransformExpression() {
    if (isSimpleStringChar(peekChar())) {
      unsupportedFeature("item transform function");
      return Optional.empty();
    }

    if (nextChar() != '\'') {
      return Optional.empty();
    }

    String expression = parseTopLevel(true, false);

    if (nextChar() != '\'') {
      return Optional.empty();
    }

    return Optional.of(item -> expandMetadataValues(item, expression));
  }

  private String expandMetadataValues(MSBuildItem item, String expression) {
    return METADATA_EXPRESSION_PATTERN
        .matcher(expression)
        .replaceAll(result -> item.getMetadata(result.group(1)));
  }

  private Optional<String> parseProperty() {
    if (nextChar() != '$' || nextChar() != '(') {
      return Optional.empty();
    }

    if (peekChar() == '[') {
      unsupportedFeature("static property function");
      return Optional.empty();
    }

    var discard = skipWhitespace();
    Optional<String> ident = parseIdentifier();
    if (ident.isEmpty()) {
      return Optional.empty();
    }

    if (peekChar() == '.') {
      unsupportedFeature("string property function");
      return Optional.empty();
    }

    discard = skipWhitespace() || discard;
    if (nextChar() != ')') {
      return Optional.empty();
    }

    if (discard) {
      // Whitespace is significant inside property expressions, but it's impossible to define
      // properties with spaces, so expressions with spaces always evaluate to "".
      return Optional.of("");
    } else {
      return Optional.of(state.getProperty(ident.get()));
    }
  }

  private Optional<String> parseMetadata() {
    if (nextChar() != '%' || nextChar() != '(') {
      return Optional.empty();
    }

    var discard = skipWhitespace();
    Optional<String> ident = parseIdentifier();
    if (ident.isEmpty()) {
      return Optional.empty();
    }

    discard = skipWhitespace() || discard;

    if (nextChar() != ')') {
      return Optional.empty();
    }

    if (discard) {
      // Whitespace is significant inside metadata expressions, but it's impossible to define
      // metadata with spaces, so expressions with spaces always evaluate to "".
      return Optional.of("");
    } else if (metadataProvider != null) {
      return Optional.of(metadataProvider.apply(ident.get()));
    } else {
      return Optional.empty();
    }
  }

  private static boolean isSimpleStringStart(char character) {
    return Character.isAlphabetic(character) || character == '_';
  }

  private static boolean isSimpleStringChar(char character) {
    return isSimpleStringStart(character) || Character.isDigit(character);
  }

  private Optional<String> parseSimpleString() {
    if (nextChar() != '\'') {
      return Optional.empty();
    }

    StringBuilder builder = new StringBuilder();

    char chr;
    while ((chr = nextChar()) != '\'') {
      builder.append(chr);
    }

    return Optional.of(builder.toString());
  }

  private Optional<String> parseIdentifier() {
    if (!isSimpleStringStart(peekChar())) {
      return Optional.empty();
    }

    StringBuilder builder = new StringBuilder();

    char chr;
    while (isSimpleStringChar(chr = peekChar())) {
      builder.append(chr);
      nextChar();
    }

    return Optional.of(builder.toString());
  }

  private boolean skipWhitespace() {
    var skipped = false;
    while (Character.isWhitespace(peekChar())) {
      nextChar();
      skipped = true;
    }
    return skipped;
  }

  private void unsupportedFeature(String feature) {
    // We don't know the relative importance of this expression - it could be an expression that
    // SonarDelphi never needs to read. Making this a warning despite having a well-defined
    // behaviour for when an unsupported feature is encountered (to interpret it literally) would
    // probably be overkill.
    LOG.debug("Unsupported MSBuild feature '{}' ignored in expression: {}", feature, text);
  }
}
