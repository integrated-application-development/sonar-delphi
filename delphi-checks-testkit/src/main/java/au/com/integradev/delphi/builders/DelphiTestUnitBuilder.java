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
package au.com.integradev.delphi.builders;

public class DelphiTestUnitBuilder
    implements BuildableDelphiTestFile, DelphiTestFileBuilder<DelphiTestUnitBuilder> {

  private static final String EXTENSION = ".pas";
  private String unitName = "Test";
  private final StringBuilder declaration = new StringBuilder();
  private final StringBuilder implementation = new StringBuilder();

  @Override
  public String getSourceCode() {
    StringBuilder source = new StringBuilder();
    source.append(String.format("unit %s;\n", this.unitName));
    source.append("\n");
    source.append("interface\n");
    source.append("\n");

    if (!this.declaration.toString().isEmpty()) {
      source.append(this.declaration);
      source.append("\n");
    }

    source.append("implementation\n");
    source.append("\n");

    if (!this.implementation.toString().isEmpty()) {
      source.append(this.implementation);
      source.append("\n");
    }

    source.append("end.\n");
    return source.toString();
  }

  @Override
  public int[] getOffset(DelphiTestFileBuilderOffset offset) {
    int sectionOffset;
    int declCount = (int) this.declaration.toString().lines().count();
    int declOffset = declCount == 0 ? declCount : (declCount + 1);
    switch (offset.getSection()) {
      case Start:
        sectionOffset = 0;
        break;
      case Declaration:
        sectionOffset = 4;
        break;
      case Implementation:
        sectionOffset = 6 + declOffset;
        break;
      default:
        throw new UnsupportedOperationException("Unsupported builder offset section");
    }
    return offset.getOffsetLines(sectionOffset);
  }

  @Override
  public String getFileName() {
    return unitName + EXTENSION;
  }

  @Override
  public DelphiTestUnitBuilder appendDecl(String value) {
    declaration.append(value).append("\n");
    return this;
  }

  @Override
  public DelphiTestUnitBuilder appendImpl(String value) {
    implementation.append(value).append("\n");
    return this;
  }

  public DelphiTestUnitBuilder unitName(String unitName) {
    this.unitName = unitName;
    return this;
  }
}
