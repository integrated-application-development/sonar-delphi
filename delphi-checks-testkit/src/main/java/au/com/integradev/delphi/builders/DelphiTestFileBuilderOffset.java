/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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

import java.util.Arrays;

public interface DelphiTestFileBuilderOffset {
  DelphiTestFileBuilderSection getSection();

  int[] getOffsetLines(int offset);

  static DelphiTestFileBuilderOffset fromStart(int... lines) {
    return new DelphiTestFileBuilderOffsetImpl(DelphiTestFileBuilderSection.Start, lines);
  }

  static DelphiTestFileBuilderOffset fromDeclaration(int... lines) {
    return new DelphiTestFileBuilderOffsetImpl(DelphiTestFileBuilderSection.Declaration, lines);
  }

  static DelphiTestFileBuilderOffset fromImplementation(int... lines) {
    return new DelphiTestFileBuilderOffsetImpl(DelphiTestFileBuilderSection.Implementation, lines);
  }

  final class DelphiTestFileBuilderOffsetImpl implements DelphiTestFileBuilderOffset {
    private DelphiTestFileBuilderSection section;
    private int[] lines;

    private DelphiTestFileBuilderOffsetImpl(
        DelphiTestFileBuilderSection builderSection, int[] lines) {
      this.section = builderSection;
      this.lines = lines;
    }

    @Override
    public DelphiTestFileBuilderSection getSection() {
      return section;
    }

    @Override
    public int[] getOffsetLines(int offset) {
      return Arrays.stream(lines).map(line -> line + offset).toArray();
    }
  }
}
