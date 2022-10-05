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
package org.sonar.plugins.delphi.utils.builders;

public class DelphiTestProgramBuilder extends DelphiTestFileBuilder<DelphiTestProgramBuilder> {
  private String programName = "omTestProgram";

  @Override
  protected DelphiTestProgramBuilder getThis() {
    return this;
  }

  @Override
  public int getOffsetDecl() {
    return 2;
  }

  @Override
  public int getOffset() {
    int offset = getDeclCount() + getOffsetDecl() + 1;
    if (!getDeclaration().isEmpty()) {
      ++offset;
    }

    return offset;
  }

  @Override
  public DelphiTestProgramBuilder appendImpl(String value) {
    // Just adding a tab for pretty-printing.
    return super.appendImpl("  " + value);
  }

  @Override
  protected StringBuilder generateSourceCode() {
    StringBuilder source = new StringBuilder();
    source.append(String.format("program %s;\n", this.programName));
    source.append("\n");

    if (!getDeclaration().isEmpty()) {
      source.append(getDeclaration());
      source.append("\n");
    }

    source.append("begin\n");

    if (!getImplementation().isEmpty()) {
      source.append(this.getImplementation());
    }

    source.append("end.\n");

    return source;
  }

  @Override
  protected String getFilenamePrefix() {
    return "program";
  }

  @Override
  protected String getFileExtension() {
    return "dpr";
  }

  public DelphiTestProgramBuilder programName(String programName) {
    this.programName = programName;
    return getThis();
  }
}
