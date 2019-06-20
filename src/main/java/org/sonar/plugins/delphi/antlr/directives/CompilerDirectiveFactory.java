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
import org.sonar.plugins.delphi.antlr.directives.exceptions.CompilerDirectiveFactorySyntaxException;
import org.sonar.plugins.delphi.antlr.directives.exceptions.CompilerDirectiveFactoryUnsupportedDirectiveException;
import org.sonar.plugins.delphi.antlr.directives.impl.DefineDirective;
import org.sonar.plugins.delphi.antlr.directives.impl.ElseDirective;
import org.sonar.plugins.delphi.antlr.directives.impl.EndIfDirective;
import org.sonar.plugins.delphi.antlr.directives.impl.IfDefDirective;
import org.sonar.plugins.delphi.antlr.directives.impl.IfDirective;
import org.sonar.plugins.delphi.antlr.directives.impl.IfEndDirective;
import org.sonar.plugins.delphi.antlr.directives.impl.IncludeDirective;
import org.sonar.plugins.delphi.antlr.directives.impl.UndefineDirective;
import org.sonar.plugins.delphi.antlr.directives.impl.UnusedDirective;
import org.sonar.plugins.delphi.utils.DelphiUtils;

// TODO: Refactor this class. The logic could be simpler and easier to follow.
/**
 * Creates concrete compiler directives based on a given string. Example: {$include unit.pas} should
 * create an IncludeDirective instance
 */
public class CompilerDirectiveFactory {
  private boolean insideString;
  private boolean insideComment;
  private boolean insideCurlyComment;
  private boolean insideParenComment;

  /**
   * Produce a list of compiler directives from a string
   *
   * @param data String to parse for compiler directives
   * @return List of compiler directives
   * @throws CompilerDirectiveFactorySyntaxException when syntax exception occurs
   */
  public List<CompilerDirective> produce(String data)
      throws CompilerDirectiveFactorySyntaxException {
    List<CompilerDirective> result = new ArrayList<>();
    int directivePos = getDirectiveFirstChar(data, 0);
    while (directivePos > -1) {
      int closingBracket = getDirectiveLastChar(data, directivePos);

      try {
        CompilerDirective directive = create(data, directivePos, closingBracket);
        result.add(directive);
      } catch (CompilerDirectiveFactoryUnsupportedDirectiveException e) {
        DelphiUtils.LOG.trace(e.getMessage());
      }

      directivePos = getDirectiveFirstChar(data, directivePos + 1);
    }
    return result;
  }

  /**
   * Creates a concrete compiler directive class base on a given string
   *
   * @param data String including some compiler directive
   * @param startPosition Start position to look for a directive
   * @param endPosition End position of a directive
   * @return concrete compiler directive class
   * @throws CompilerDirectiveFactoryUnsupportedDirectiveException when not recognize the directive
   * name
   * @throws CompilerDirectiveFactorySyntaxException when no compiler directive could be created
   */
  public CompilerDirective create(String data, int startPosition, int endPosition)
      throws CompilerDirectiveFactoryUnsupportedDirectiveException,
      CompilerDirectiveFactorySyntaxException {
    int directiveFirstChar = getDirectiveFirstChar(data, startPosition);
    int directiveLastChar = getDirectiveLastChar(data, startPosition);
    String directiveName = getName(data, directiveFirstChar);
    String directiveItem = getItem(data, directiveFirstChar);
    CompilerDirectiveType type = CompilerDirectiveType.getTypeByName(directiveName.toLowerCase());

    switch (type) {
      case DEFINE:
        return new DefineDirective(directiveItem, directiveFirstChar, directiveLastChar);
      case UNDEFINE:
        return new UndefineDirective(directiveItem, directiveFirstChar, directiveLastChar);
      case IF:
        return new IfDirective(directiveItem, directiveFirstChar, directiveLastChar);
      case IFDEF:
        return new IfDefDirective(directiveName, directiveItem, directiveFirstChar,
            directiveLastChar);
      case IFEND:
        return new IfEndDirective(directiveItem, directiveFirstChar, directiveLastChar);
      case ENDIF:
        return new EndIfDirective(directiveItem, directiveFirstChar, directiveLastChar);
      case ELSE:
        return new ElseDirective(directiveItem, directiveFirstChar, directiveLastChar);
      case INCLUDE:
        return new IncludeDirective(directiveItem, directiveFirstChar, directiveLastChar);
      case UNUSED:
        return new UnusedDirective(directiveFirstChar, directiveLastChar);
      default:
        throw new CompilerDirectiveFactoryUnsupportedDirectiveException(
            "Not implemented directive name: "
                + directiveName);
    }
  }

  private int getDirectiveFirstChar(String data, int startPosition) {
    for (int i = startPosition; i < data.length(); ++i) {
      if (notInsideCommentOrString() && data.charAt(i) == '{' && peekChar(data, i) == '$') {
        return i;
      }

      handleCharacter(data, i);
    }

    return -1;
  }

  private void handleCharacter(String data, int position) {
    handleComments(data, position);
    handleString(data, position);
  }

  private void handleComments(String data, int position) {
    if (insideString) {
      return;
    }

    switch (data.charAt(position)) {
      case '/':
        if (peekChar(data, position) == '/') {
          insideComment = true;
        }
        break;

      case '(':
        if (peekChar(data, position) == '*') {
          insideParenComment = true;
        }
        break;

      case ')':
        if (prevChar(data, position) == '*') {
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

  private void handleString(String data, int position) {
    if (insideComment()) {
      return;
    }

    if (data.charAt(position) == '\'') {
      insideString = !insideString;
    }
  }

  private char peekChar(String data, int position) {
    return (data.length() > position + 1) ? data.charAt(position + 1) : ' ';
  }

  private char prevChar(String data, int position) {
    return position != 0 ? data.charAt(position - 1) : ' ';
  }

  private int getDirectiveLastChar(String data, int startPosition)
      throws CompilerDirectiveFactorySyntaxException {
    for (int i = startPosition; i < data.length(); ++i) {
      if (notInsideCommentOrString() && data.charAt(i) == '}') {
        return i;
      }

      handleCharacter(data, i);
    }

    throw new CompilerDirectiveFactorySyntaxException(
        "No closing bracket for compiler directive from: " + startPosition + " in: " + data);
  }

  private boolean notInsideCommentOrString() {
    return !insideComment() && !insideString;
  }

  private boolean insideComment() {
    return insideComment || insideCurlyComment || insideParenComment;
  }

  private String getItem(String data, int startPos) throws CompilerDirectiveFactorySyntaxException {
    int pos = data.indexOf(' ', startPos + 1);
    int endPos = getDirectiveLastChar(data, startPos);
    if (pos > -1 && pos < endPos) {
      return data.substring(pos + 1, data.indexOf('}', pos)).trim();
    }
    return "";
  }

  private String getName(String data, int startPos) throws CompilerDirectiveFactorySyntaxException {
    if (startPos > -1) {
      int endPos = getDirectiveLastChar(data, startPos);
      int itemPos = data.indexOf(' ', startPos);

      // we have an item in
      if (itemPos < endPos && itemPos > -1) {
        // directive
        return data.substring(startPos + 2, itemPos).trim();
      } else {
        return data.substring(startPos + 2, endPos).trim();
      }
    }
    throw new CompilerDirectiveFactorySyntaxException(
        "Could not get compiler definition name for: " + data);
  }

}
