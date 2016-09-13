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
package org.sonar.plugins.delphi.antlr.ast;

import org.sonar.plugins.delphi.antlr.ast.exceptions.NodeNameForCodeDoesNotExistException;

public enum NodeName {
  UNKNOWN("unknown", ""),
  SEMI("semi", ";"),
  DOT("dot", "\\."),
  COLON("colon", "[:()]"),
  GUID_IDENT("guid_ident", "^'\\{.*"),
  DASH("dash", ","),
  DASH_POINTER("dash_pointer", "\\^");

  private String name;
  private String codeRegExpression;

  NodeName(String name, String regex) {
    this.name = name;
    this.codeRegExpression = regex;
  }

  public boolean matchesCode(String code) {
    return code.matches(codeRegExpression);
  }

  static NodeName findByCode(String code) {
    for (NodeName nodeName : NodeName.values()) {
      if (nodeName.matchesCode(code)) {
        return nodeName;
      }
    }
    throw new NodeNameForCodeDoesNotExistException(code);
  }

  public String getName() {
    return name;
  }
}
