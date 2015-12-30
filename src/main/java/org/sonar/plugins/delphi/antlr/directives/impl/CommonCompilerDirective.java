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

import org.sonar.plugins.delphi.antlr.directives.CompilerDirective;
import org.sonar.plugins.delphi.antlr.directives.CompilerDirectiveType;

/**
 * Common functionality for all compiler directives
 */
public class CommonCompilerDirective implements CompilerDirective {

  protected String name;
  protected String item;
  protected int firstCharPos;
  protected int lastCharPos;

  /**
   * ctor
   * 
   * @param name directive name
   * @param item directive item
   * @param firstCharPos directive first character occurrence
   * @param lastCharPos directive last character occurrence
   * @throws IllegalArgumentException if name was null, or firstCharPos &lt; 0, or firstCharPos &lt; lastCharPos
   */
  public CommonCompilerDirective(String name, String item, int firstCharPos, int lastCharPos) {
    assertValues(name, firstCharPos, lastCharPos);
    this.name = name;
    this.item = item;
    this.firstCharPos = firstCharPos;
    this.lastCharPos = lastCharPos;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getItem() {
    return item;
  }

  @Override
  public int getFirstCharPosition() {
    return firstCharPos;
  }

  @Override
  public int getLastCharPosition() {
    return lastCharPos;
  }

  @Override
  public CompilerDirectiveType getType() {
    return CompilerDirectiveType.UNKNOWN;
  }

  private void assertValues(String name, int beginPos, int endPos) {
    if (name == null) {
      throw new IllegalArgumentException("Compiler directive name cannot be null!");
    }
    if (beginPos < 0 || endPos < 0) {
      throw new IllegalArgumentException("Compiler directive first/last character occurence must be >= 0");
    }
    if (beginPos > endPos) {
      throw new IllegalArgumentException("Compiler directive first character must be <= last character");
    }

  }

  @Override
  public String toString() {
    return name + " " + item;
  }

  @Override
  public int getLength() {
    return lastCharPos - firstCharPos;
  }

}
