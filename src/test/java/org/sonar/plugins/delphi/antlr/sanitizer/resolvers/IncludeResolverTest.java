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
import org.sonar.plugins.delphi.debug.FileTestsCommon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class IncludeResolverTest extends FileTestsCommon {

  private static final String FILE_NAME = "/org/sonar/plugins/delphi/grammar/GrammarTest.pas";
  private IncludeResolver resolver;

  @BeforeClass
  public static void init() throws IOException {
    loadFile(FILE_NAME);
  }

  @Before
  public void setup() {
    resolver = new IncludeResolver(true, new ArrayList<File>());
  }

  @Test
  public void resolveIncludesTest() {
    SourceResolverResults results = new SourceResolverResults(testFile.getAbsolutePath(), testFileString);
    ExcludeResolver excludeResolver = new ExcludeResolver();
    excludeResolver.resolve(results);

    resolver.resolve(results);
    assertEquals(4, resolver.getIncludedFilesPath().size());
  }

}
