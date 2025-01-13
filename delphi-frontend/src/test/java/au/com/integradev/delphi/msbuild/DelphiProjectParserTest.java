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
package au.com.integradev.delphi.msbuild;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.enviroment.EnvironmentVariableProvider;
import au.com.integradev.delphi.utils.DelphiUtils;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DelphiProjectParserTest {

  private static final String SIMPLE_PROJECT =
      "/au/com/integradev/delphi/projects/SimpleProject/dproj/SimpleDelphiProject.dproj";

  private static final String OPT_SET_PROJECT = "/au/com/integradev/delphi/msbuild/OptSet.dproj";

  private static final String BAD_OPT_SET_PROJECT =
      "/au/com/integradev/delphi/msbuild/BadOptSet.dproj";

  private static final String BAD_UNIT_ALIAS_PROJECT =
      "/au/com/integradev/delphi/msbuild/BadUnitAlias.dproj";

  private static final String BAD_SEARCH_PATH_PROJECT =
      "/au/com/integradev/delphi/msbuild/BadSearchPath.dproj";

  private static final String BAD_SOURCE_FILE_PROJECT =
      "/au/com/integradev/delphi/msbuild/BadSourceFile.dproj";

  private static final String LIBRARY_PATH_PROJECT =
      "/au/com/integradev/delphi/msbuild/LibraryPath.dproj";

  private static final String BROWSING_PATH_PROJECT =
      "/au/com/integradev/delphi/msbuild/BrowsingPath.dproj";

  private EnvironmentVariableProvider environmentVariableProvider;
  private Path environmentProj;

  private DelphiProject parse(String resource) {
    Path dproj = DelphiUtils.getResource(resource).toPath();
    DelphiProjectParser parser =
        new DelphiProjectParser(dproj, environmentVariableProvider, environmentProj);
    return parser.parse();
  }

  @BeforeEach
  void init() {
    environmentVariableProvider = mock(EnvironmentVariableProvider.class);
    when(environmentVariableProvider.getenv()).thenReturn(Collections.emptyMap());
    when(environmentVariableProvider.getenv(anyString())).thenReturn(null);
    environmentProj = null;
  }

  @Test
  void testSimpleProjectFile() {
    DelphiProject project = parse(SIMPLE_PROJECT);

    assertThat(project.getSourceFiles()).hasSize(8);

    String[] fileNames = {
      "Globals.pas",
      "MainWindow.pas",
      "OverloadTest.pas",
      "StatementTest.pas",
      "CommentsTest.pas",
      "AccessorsTest.pas",
      "FunctionTest.pas",
      "GlobalsTest.pas"
    };

    for (int i = 0; i < fileNames.length; ++i) {
      assertThat(project.getSourceFiles().get(i)).hasFileName(fileNames[i]);
    }

    assertThat(project.getSearchDirectories()).hasSize(3);
    assertThat(project.getDebugSourceDirectories()).hasSize(1);

    assertThat(project.getConditionalDefines())
        .containsOnly("MSWINDOWS", "CPUX86", "DEBUG", "GGMSGDEBUGx", "LOGTOFILEx", "FullDebugMode");

    assertThat(project.getUnitScopeNames()).containsOnly("Vcl", "System");

    assertThat(project.getUnitAliases())
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "DbiErrs",
                "BDE",
                "DbiProcs",
                "BDE",
                "DbiTypes",
                "BDE",
                "WinProcs",
                "Windows",
                "WinTypes",
                "Windows"));
  }

  @Test
  void testOptSetProject() {
    DelphiProject project = parse(OPT_SET_PROJECT);
    assertThat(project.getUnitAliases())
        .containsExactlyInAnyOrderEntriesOf(Map.of("WinProcs", "Windows", "WinTypes", "Windows"));
  }

  @Test
  void testBadOptSetProjectShouldContainValidOptsetValues() {
    DelphiProject project = parse(BAD_OPT_SET_PROJECT);
    assertThat(project.getUnitAliases())
        .containsExactlyInAnyOrderEntriesOf(Map.of("WinProcs", "Windows", "WinTypes", "Windows"));
  }

  @Test
  void testBadUnitAliasProjectShouldContainValidAliases() {
    DelphiProject project = parse(BAD_UNIT_ALIAS_PROJECT);

    assertThat(project.getUnitAliases())
        .containsExactlyInAnyOrderEntriesOf(Map.of("ValidAlias", "ValidUnit"));
  }

  @Test
  void testBadSearchPathProjectShouldContainValidSearchPaths() {
    DelphiProject project = parse(BAD_SEARCH_PATH_PROJECT);

    assertThat(project.getSearchDirectories())
        .containsOnly(DelphiUtils.getResource("/au/com/integradev/delphi/msbuild").toPath());
  }

  @Test
  void testBadSourceFileProjectShouldContainValidSourceFiles() {
    DelphiProject project = parse(BAD_SOURCE_FILE_PROJECT);

    assertThat(project.getSourceFiles())
        .containsOnly(DelphiUtils.getResource("/au/com/integradev/delphi/file/Empty.pas").toPath());
  }

  @Test
  void testLibraryPathProject() {
    DelphiProject project = parse(LIBRARY_PATH_PROJECT);

    assertThat(project.getLibraryPathDirectories())
        .containsExactly(
            DelphiUtils.getResource("/au/com/integradev/delphi").toPath(),
            DelphiUtils.getResource("/au/com/integradev").toPath());
  }

  @Test
  void testBrowsingPathProject() {
    DelphiProject project = parse(BROWSING_PATH_PROJECT);

    assertThat(project.getBrowsingPathDirectories())
        .containsExactly(DelphiUtils.getResource("/au/com/integradev/delphi").toPath());
  }
}
