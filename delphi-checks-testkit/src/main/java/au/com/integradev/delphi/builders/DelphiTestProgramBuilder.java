/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.builders;

public final class DelphiTestProgramBuilder
    implements BuildableDelphiTestFile, DelphiTestFileBuilder<DelphiTestProgramBuilder> {
  private String programName = "omTestProgram";
  private final StringBuilder declaration = new StringBuilder();
  private final StringBuilder implementation = new StringBuilder();

  @Override
  public DelphiTestProgramBuilder appendImpl(String value) {
    // Just adding a tab for pretty-printing.
    implementation.append("  ").append(value).append("\n");
    return this;
  }

  @Override
  public DelphiTestProgramBuilder appendDecl(String value) {
    declaration.append(value).append("\n");
    return this;
  }

  @Override
  public String getSourceCode() {
    StringBuilder source = new StringBuilder();
    source.append(String.format("program %s;\n", this.programName));
    source.append("\n");

    if (!this.declaration.toString().isEmpty()) {
      source.append(this.declaration);
      source.append("\n");
    }

    source.append("begin\n");

    if (!this.implementation.toString().isEmpty()) {
      source.append(this.implementation);
    }

    source.append("end.\n");
    return source.toString();
  }

  @Override
  public String getFileName() {
    return programName;
  }

  @Override
  public String getExtension() {
    return "dpr";
  }

  public DelphiTestProgramBuilder programName(String value) {
    this.programName = value;
    return this;
  }

  @Override
  public DelphiTestProgramBuilder unitName(String value) {
    this.programName = value;
    return this;
  }
}
