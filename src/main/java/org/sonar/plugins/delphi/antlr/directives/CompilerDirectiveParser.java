/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
package org.sonar.plugins.delphi.antlr.directives;

import java.util.ArrayList;
import java.util.List;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.antlr.directives.exceptions.CompilerDirectiveSyntaxException;
import org.sonar.plugins.delphi.antlr.directives.exceptions.UnsupportedCompilerDirectiveException;

/**
 * Parses a list of compiler directives out of a given string. Example: "{$include unit.pas}" will
 * create 1 IncludeDirective instance
 */
public class CompilerDirectiveParser {
  private static final Logger LOG = Loggers.get(CompilerDirectiveParser.class);
  private static final char END_OF_INPUT = '\0';

  private enum DirectiveBracketType {
    CURLY,
    PAREN
  }

  // Parser state
  private String data;
  private int position;
  private List<CompilerDirective> directives;
  private boolean insideString;
  private boolean insideComment;
  private boolean insideCurlyComment;
  private boolean insideParenComment;

  // Current directive state
  private DirectiveBracketType directiveBracketType;
  private final StringBuilder directiveName = new StringBuilder();
  private final StringBuilder directiveItem = new StringBuilder();
  private int directiveStartPos;

  /**
   * Produce a list of compiler directives from a string
   *
   * @param data String to parse for compiler directives
   * @return List of compiler directives
   * @throws CompilerDirectiveSyntaxException when syntax exception occurs
   */
  public List<CompilerDirective> parse(String data) {
    this.data = data;
    this.position = 0;
    this.directives = new ArrayList<>();
    this.insideString = false;
    this.insideComment = false;
    this.insideCurlyComment = false;
    this.insideParenComment = false;

    while (position < data.length()) {
      handleCharacter();
      ++position;
    }

    return this.directives;
  }

  private void handleCharacter() {
    handleDirective();
    handleComments();
    handleString();
  }

  private void handleDirective() {
    if (insideCommentOrString()) {
      return;
    }

    switch (getChar(position)) {
      case '{':
        if (peekChar() == '$') {
          parseDirective(DirectiveBracketType.CURLY);
        }
        break;

      case '(':
        if (peekChar(1) == '*' && peekChar(2) == '$') {
          parseDirective(DirectiveBracketType.PAREN);
        }
        break;

      default:
        // Do nothing
    }
  }

  private void parseDirective(DirectiveBracketType bracketType) {
    this.directiveBracketType = bracketType;
    this.directiveStartPos = position;

    if (bracketType == DirectiveBracketType.CURLY) {
      position += 2;
      // Jump ahead of the "{$"
    }

    if (bracketType == DirectiveBracketType.PAREN) {
      position += 3;
      // Jump ahead of the "(*$"
    }

    parseDirectiveName();
    parseDirectiveItem();
    handleEndOfDirective();
  }

  private void handleEndOfDirective() {
    String name = directiveName.toString().trim();
    String item = directiveItem.toString().trim();

    if (directiveBracketType == DirectiveBracketType.PAREN) {
      ++position;
    }

    try {
      directives.add(CompilerDirective.create(name, item, directiveStartPos, position));
    } catch (UnsupportedCompilerDirectiveException e) {
      LOG.trace("Failed to parse Compiler Directive: ", e);
    }
  }

  private void parseDirectiveName() {
    directiveName.setLength(0);
    char character = getChar(position);

    while (character != END_OF_INPUT) {
      if (Character.isWhitespace(character) || isEndOfDirective(character)) {
        return;
      }

      directiveName.append(character);
      character = getChar(++position);
    }

    throw new CompilerDirectiveSyntaxException("Unexpected end of input");
  }

  private void parseDirectiveItem() {
    directiveItem.setLength(0);
    char character = getChar(position);

    while (character != END_OF_INPUT) {
      if (isEndOfDirective(character)) {
        return;
      }

      directiveItem.append(character);
      character = getChar(++position);
    }

    throw new CompilerDirectiveSyntaxException("Unexpected end of input");
  }

  private boolean isEndOfDirective(char character) {
    boolean result = false;

    if (directiveBracketType == DirectiveBracketType.CURLY) {
      result = (character == '}');
    }

    if (directiveBracketType == DirectiveBracketType.PAREN) {
      result = (character == '*' && peekChar() == ')');
    }

    return result;
  }

  private void handleComments() {
    if (insideString) {
      return;
    }

    handleCommentStart();
    handleCommentEnd();
  }

  private void handleCommentStart() {
    switch (getChar(position)) {
      case '/':
        if (peekChar() == '/') {
          insideComment = !insideCurlyComment && !insideParenComment;
        }
        break;

      case '(':
        if (peekChar() == '*') {
          insideParenComment = true;
        }
        break;

      case '{':
        insideCurlyComment = true;
        break;

      default:
        // Do nothing
    }
  }

  private void handleCommentEnd() {
    switch (getChar(position)) {
      case ')':
        if (prevChar() == '*') {
          insideParenComment = false;
        }
        break;

      case '}':
        insideCurlyComment = false;
        break;

      case '\n':
        insideComment = false;
        break;

      default:
        // Do nothing
    }
  }

  private void handleString() {
    if (insideComment()) {
      return;
    }

    if (getChar(position) == '\'') {
      insideString = !insideString;
    }
  }

  private char peekChar() {
    return peekChar(1);
  }

  private char peekChar(int offset) {
    return getChar(position + offset);
  }

  private char getChar(int position) {
    return (position < data.length()) ? data.charAt(position) : END_OF_INPUT;
  }

  private char prevChar() {
    return getChar(position - 1);
  }

  private boolean insideCommentOrString() {
    return insideComment() || insideString;
  }

  private boolean insideComment() {
    return insideComment || insideCurlyComment || insideParenComment;
  }
}
