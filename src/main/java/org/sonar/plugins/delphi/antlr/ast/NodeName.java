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

public enum NodeName {
  UNKNOWN("unknown", ""),
  SEMI("semi", ";"),
  EQUALS("equals", ":="),
  NOT_EQUALS("not_equals", "<>"),
  PLUS("plus", "\\+"),
  MINUS("minus", "-"),
  DIVIDE("minus", "/"),
  MULTIPLY("minus", "\\*"),
  L_SQUARE_BRACKET("l_square_bracket", "\\["),
  R_SQUARE_BRACKET("r_square_bracket", "\\]"),
  STRING("string", "'.'"),
  DOT("dot", "\\."),
  DOTDOT("dotdot", "\\.\\."),
  INTNUM("int_num", "[0-9]+"),
  REALNUM("real_num", "([0-9]|\\.)+"),
  COLON("colon", "[:()]"),
  GUID_IDENT("guid_ident", "^'\\{.*"),
  DASH("dash", ","),
  DASH_POINTER("dash_pointer", "\\^");

  private final String name;
  private final String codeRegExpression;

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
    return UNKNOWN;
  }

  public String getName() {
    return name;
  }
}
