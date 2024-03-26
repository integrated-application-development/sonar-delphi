/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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
package au.com.integradev.delphi.checks.verifier;

class TextEditExpectation {

  public TextEditExpectation(
      String fixId,
      String replacement,
      int beginLine,
      int endLine,
      int beginColumn,
      int endColumn) {
    this.fixId = fixId;
    this.beginLine = beginLine;
    this.endLine = endLine;
    this.beginColumn = beginColumn;
    this.endColumn = endColumn;
    this.replacement = replacement;
  }

  private final String fixId;
  private final int beginLine;
  private final int endLine;
  private final int beginColumn;
  private final int endColumn;
  private final String replacement;

  public String getFixId() {
    return fixId;
  }

  public int getBeginLine() {
    return beginLine;
  }

  public int getEndLine() {
    return endLine;
  }

  public int getBeginColumn() {
    return beginColumn;
  }

  public int getEndColumn() {
    return endColumn;
  }

  public String getReplacement() {
    return replacement;
  }
}
