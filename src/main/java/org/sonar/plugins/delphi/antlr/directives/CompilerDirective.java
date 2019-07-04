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

import org.sonar.plugins.delphi.antlr.directives.exceptions.UnsupportedCompilerDirectiveException;
import org.sonar.plugins.delphi.antlr.directives.exceptions.CompilerDirectiveSyntaxException;
import org.sonar.plugins.delphi.antlr.directives.impl.DefineDirective;
import org.sonar.plugins.delphi.antlr.directives.impl.ElseDirective;
import org.sonar.plugins.delphi.antlr.directives.impl.EndIfDirective;
import org.sonar.plugins.delphi.antlr.directives.impl.IfDefDirective;
import org.sonar.plugins.delphi.antlr.directives.impl.IfDirective;
import org.sonar.plugins.delphi.antlr.directives.impl.IfEndDirective;
import org.sonar.plugins.delphi.antlr.directives.impl.IncludeDirective;
import org.sonar.plugins.delphi.antlr.directives.impl.UndefineDirective;
import org.sonar.plugins.delphi.antlr.directives.impl.UnusedDirective;

/**
 * Compiler directive interface
 */
public interface CompilerDirective {

  /**
   * example: <code>#include &lt;iostream&gt;</code> will return "include"
   *
   * @return definition name
   */
  String getName();

  /**
   * example: <code>#include &lt;iostream&gt;</code> will return "iostream"
   *
   * @return definition item
   */
  String getItem();

  /**
   * @return first definition char position in whole file
   */
  int getFirstCharPosition();

  /**
   * @return last definition char position
   */
  int getLastCharPosition();

  /**
   * @return preprocessor definition type
   */
  CompilerDirectiveType getType();

  /**
   * Get length of the directive, that is: the span from first and last char position
   *
   * @return directive length
   */
  int getLength();


  /**
   * Creates a concrete compiler directive class base on a given string
   *
   * @param name Name of the directive
   * @param item Argument(s) following the name of the directive
   * @param startPos Start position of the compiler directive in the input string
   * @param endPos End position of the compiler directive in the input string
   * @return concrete compiler directive class
   *
   * @throws UnsupportedCompilerDirectiveException when directive name is unknown
   * @throws CompilerDirectiveSyntaxException when no compiler directive could be created
   */
  static CompilerDirective create(String name, String item, int startPos, int endPos) {
    CompilerDirectiveType type = CompilerDirectiveType.getTypeByName(name.toLowerCase());

    switch (type) {
      case DEFINE:
        return new DefineDirective(item, startPos, endPos);
      case UNDEFINE:
        return new UndefineDirective(item, startPos, endPos);
      case IF:
        return new IfDirective(item, startPos, endPos);
      case IFDEF:
        return new IfDefDirective(name, item, startPos, endPos);
      case IFEND:
        return new IfEndDirective(item, startPos, endPos);
      case ENDIF:
        return new EndIfDirective(item, startPos, endPos);
      case ELSE:
        return new ElseDirective(item, startPos, endPos);
      case INCLUDE:
        return new IncludeDirective(item, startPos, endPos);
      case UNUSED:
        return new UnusedDirective(startPos, endPos);
      default:
        throw new UnsupportedCompilerDirectiveException("Not implemented directive name: " + name);
    }
  }
}
