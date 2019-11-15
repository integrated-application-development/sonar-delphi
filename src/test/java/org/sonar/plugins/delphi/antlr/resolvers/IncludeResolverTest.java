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
package org.sonar.plugins.delphi.antlr.resolvers;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class IncludeResolverTest {
  private static final File TEST_FILE =
      DelphiUtils.getResource("/org/sonar/plugins/delphi/grammar/GrammarTest.pas");

  private IncludeResolver resolver;

  @Before
  public void setup() {
    resolver = new IncludeResolver(true, new ArrayList<>());
  }

  @Test
  public void testResolveIncludes() throws IOException {
    String testFileString = DelphiUtils.readFileContent(TEST_FILE, UTF_8.name());
    SourceResolverResults results =
        new SourceResolverResults(TEST_FILE.getAbsolutePath(), new StringBuilder(testFileString));

    resolver.resolve(results);
    assertThat(resolver.getIncludedFilesPath().size()).isEqualTo(4);
  }
}
