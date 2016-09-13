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
package org.sonar.plugins.delphi.antlr.sanitizer.resolvers;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.SubRange;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.impl.IntegerSubRange;
import org.sonar.plugins.delphi.debug.FileTestsCommon;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExcludeResolverTest extends FileTestsCommon {

  private static final String FILE_NAME = "/org/sonar/plugins/delphi/syntax/SyntaxTest.pas";
  private static final int SUBRANGES_COUNT = 7;
  private ExcludeResolver resolver;

  @BeforeClass
  public static void init() throws IOException {
    loadFile(FILE_NAME);
  }

  @Before
  public void setup() {
    StringBuilder data = new StringBuilder();
    data.append(testFileString);
    resolver = new ExcludeResolver();
  }

  @Test
  public void getAllExcludesTest() {
    SubRange expectedResults[] = {new IntegerSubRange(42, 60), new IntegerSubRange(62, 118),
      new IntegerSubRange(329, 365),
      new IntegerSubRange(375, 400), new IntegerSubRange(402, 411), new IntegerSubRange(420, 436),
      new IntegerSubRange(449, 478)};

    SourceResolverResults result = new SourceResolverResults(testFile.getAbsolutePath(), testFileString);
    resolver.resolve(result);

    List<SubRange> excludes = result.getFileExcludes().getRanges();
    assertEquals(SUBRANGES_COUNT, excludes.size());

    for (int i = 0; i < expectedResults.length; ++i) {
      assertEquals(expectedResults[i], excludes.get(i));
    }
  }
}
