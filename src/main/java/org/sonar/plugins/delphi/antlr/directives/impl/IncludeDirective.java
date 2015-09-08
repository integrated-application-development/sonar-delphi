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
package org.sonar.plugins.delphi.antlr.directives.impl;

import org.sonar.plugins.delphi.antlr.directives.CompilerDirectiveType;

/**
 * {$include ...} or {$i ...} directive
 */
public class IncludeDirective extends CommonCompilerDirective {

  private static final String DIRECTIVE_NAME = "include";

  /**
   * ctor
   * 
   * @param item directive item
   * @param firstCharPos first character position
   * @param lastCharPos last character position
   */
  public IncludeDirective(String item, int firstCharPos, int lastCharPos) {
    super(DIRECTIVE_NAME, item, firstCharPos, lastCharPos);
  }

  @Override
  public CompilerDirectiveType getType() {
    return CompilerDirectiveType.INCLUDE;
  }

  @Override
  public String getItem() {
    String result = String.copyValueOf(item.toCharArray());
    // get rid of ' ', \ -> /
    result = result.replaceAll(" ", "").replaceAll("\\\\", "/");
    // get rid of '../'
    result = result.replaceAll("\\.\\./", "");
    return result;
  }
}
