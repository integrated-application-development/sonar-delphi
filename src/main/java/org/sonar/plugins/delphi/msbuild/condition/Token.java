/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package org.sonar.plugins.delphi.msbuild.condition;

public class Token {
  enum TokenType {
    COMMA,
    LPAREN,
    RPAREN,
    LESS_THAN,
    GREATER_THAN,
    LESS_THAN_EQUAL,
    GREATER_THAN_EQUAL,
    AND,
    OR,
    EQUAL,
    NOT_EQUAL,
    NOT,
    PROPERTY,
    STRING,
    NUMERIC,
    FUNCTION,
    END_OF_INPUT
  }

  private final TokenType type;
  private final String text;
  private final boolean expandable;

  public Token(TokenType type, String text, boolean expandable) {
    this.type = type;
    this.text = text;
    this.expandable = expandable;
  }

  public Token(TokenType type, String text) {
    this(type, text, false);
  }

  public TokenType getType() {
    return type;
  }

  public String getText() {
    return text;
  }

  public boolean getExpandable() {
    return expandable;
  }
}
