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
package org.sonar.plugins.delphi.msbuild;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.enviroment.EnvironmentVariableProvider;
import org.sonar.plugins.delphi.utils.DelphiUtils;

class DelphiProjectParserTest {

  private static final String SIMPLE_PROJECT =
      "/org/sonar/plugins/delphi/projects/SimpleProject/dproj/SimpleDelphiProject.dproj";

  private static final String OPT_SET_PROJECT = "/org/sonar/plugins/delphi/msbuild/OptSet.dproj";

  private static final String BAD_OPT_SET_PROJECT =
      "/org/sonar/plugins/delphi/msbuild/BadOptSet.dproj";

  private static final String BAD_UNIT_ALIAS_PROJECT =
      "/org/sonar/plugins/delphi/msbuild/BadUnitAlias.dproj";

  private static final String BAD_SEARCH_PATH_PROJECT =
      "/org/sonar/plugins/delphi/msbuild/BadSearchPath.dproj";

  private static final String BAD_SOURCE_FILE_PROJECT =
      "/org/sonar/plugins/delphi/msbuild/BadSourceFile.dproj";

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

    assertThat(project.getSearchDirectories()).hasSize(2);
    assertThat(project.getDebugSourceDirectories()).hasSize(1);

    String[] includeNames = {"includes1", "includes2"};
    for (int i = 0; i < includeNames.length; ++i) {
      assertThat(project.getSearchDirectories().get(i).getFileName()).hasToString(includeNames[i]);
    }

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
        .containsOnly(DelphiUtils.getResource("/org/sonar/plugins/delphi/msbuild").toPath());
  }

  @Test
  void testBadSourceFileProjectShouldContainValidSourceFiles() {
    DelphiProject project = parse(BAD_SOURCE_FILE_PROJECT);

    assertThat(project.getSourceFiles())
        .containsOnly(DelphiUtils.getResource("/org/sonar/plugins/delphi/file/Empty.pas").toPath());
  }
}
