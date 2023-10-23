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
    extends AbstractDelphiTestFileBuilder<DelphiTestProgramBuilder> {
  private static final String EXTENSION = ".dpr";
  private String programName = "TestProgram";

  @Override
  protected DelphiTestProgramBuilder getThis() {
    return this;
  }

  @Override
  public DelphiTestProgramBuilder appendImpl(String value) {
    return super.appendImpl("  " + value);
  }

  @Override
  public String sourceCode() {
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
    return programName + EXTENSION;
  }

  public DelphiTestProgramBuilder programName(String value) {
    this.programName = value;
    return this;
  }
}
