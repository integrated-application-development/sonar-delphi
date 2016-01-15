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

/**
 * Creates concrete compiler directives based on a given string. Example:
 * {$include unit.pas} should create a IncludeDirective instance
 * 
 */
public class CompilerDirectiveFactory {

  /**
   * Produce a list of compiler directives from a string
   * 
   * @param data String to parse for compiler directives
   * @return List of compiler directives
   * @throws CompilerDirectiveFactorySyntaxException when syntax exception
   *             occurs
   */
  public List<CompilerDirective> produce(String data) throws CompilerDirectiveFactorySyntaxException {
    List<CompilerDirective> result = new ArrayList<CompilerDirective>();
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
   * @throws CompilerDirectiveFactoryUnsupportedDirectiveException when not recognize the directive name
   * @throws CompilerDirectiveFactorySyntaxException when no compiler directive could be created
   */
  public CompilerDirective create(String data, int startPosition, int endPosition)
    throws CompilerDirectiveFactoryUnsupportedDirectiveException, CompilerDirectiveFactorySyntaxException {
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
        return new IfDefDirective(directiveName, directiveItem, directiveFirstChar, directiveLastChar);
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
        throw new CompilerDirectiveFactoryUnsupportedDirectiveException("Not implemented directive name: "
          + directiveName);
    }
  }

  private int getDirectiveFirstChar(String data, int startPosition) throws CompilerDirectiveFactorySyntaxException {
    return data.indexOf("{$", startPosition);
  }

  private int getDirectiveLastChar(String data, int startPosition) throws CompilerDirectiveFactorySyntaxException {
    int pos = data.indexOf("}", startPosition + 1);
    if (pos == -1) {
      throw new CompilerDirectiveFactorySyntaxException("No closing bracket for compiler directive from: "
        + startPosition + " in: " + data);
    }
    return pos;
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
      if (itemPos < endPos && itemPos > -1) { // we have an item in
                                              // directive
        return data.substring(startPos + 2, itemPos).trim();
      } else {
        return data.substring(startPos + 2, endPos).trim();
      }
    }
    throw new CompilerDirectiveFactorySyntaxException("Could not get compiler definition name for: " + data);
  }

}
