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
package org.sonar.plugins.delphi.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiProjectHelperTest {
  private static final String PROJECTS_PATH = "/org/sonar/plugins/delphi/projects/";
  private static final File BASE_DIR = DelphiUtils.getResource(PROJECTS_PATH);
  private Configuration settings;
  private DefaultFileSystem fs;

  @Before
  public void setup() {
    fs = new DefaultFileSystem(BASE_DIR);
    settings = mock(Configuration.class);

    String[] includes = {"BadSyntaxProject"};
    when(settings.getStringArray(DelphiPlugin.SEARCH_PATH_KEY)).thenReturn(includes);

    String[] defines = {"DefineFromSettings"};
    when(settings.getStringArray(DelphiPlugin.CONDITIONAL_DEFINES_KEY)).thenReturn(defines);
  }

  @Test
  public void testInvalidIncludesShouldBeSkipped() {
    String[] includes = {"EmptyProject/empty", "BadSyntaxProject", "BadPath/Spooky"};
    when(settings.getStringArray(DelphiPlugin.SEARCH_PATH_KEY)).thenReturn(includes);

    DelphiProjectHelper delphiProjectHelper = new DelphiProjectHelper(settings, fs);

    assertThat(delphiProjectHelper.getSearchDirectories()).hasSize(1);
  }

  @Test
  public void testDprojProject() {
    InputFile inputFile =
        TestInputFileBuilder.create(
                DelphiLanguage.KEY,
                BASE_DIR,
                DelphiUtils.getResource(
                    PROJECTS_PATH + "SimpleProject/dproj/SimpleDelphiProject.dproj"))
            .build();
    fs.add(inputFile);

    DelphiProjectHelper delphiProjectHelper = new DelphiProjectHelper(settings, fs);

    assertThat(delphiProjectHelper.getSearchDirectories()).hasSize(3);
    assertThat(delphiProjectHelper.getConditionalDefines())
        .hasSize(8)
        .contains(
            "MSWINDOWS",
            "CPUX86",
            "DEBUG",
            "GGMSGDEBUGx",
            "LOGTOFILEx",
            "FullDebugMode",
            "RELEASE",
            "DefineFromSettings");
  }

  @Test
  public void testWorkgroupProject() {
    InputFile inputFile =
        TestInputFileBuilder.create(
                DelphiLanguage.KEY,
                BASE_DIR,
                DelphiUtils.getResource(PROJECTS_PATH + "SimpleProject/workgroup/All.groupproj"))
            .build();
    fs.add(inputFile);

    DelphiProjectHelper delphiProjectHelper = new DelphiProjectHelper(settings, fs);

    assertThat(delphiProjectHelper.getSearchDirectories()).hasSize(3);
    assertThat(delphiProjectHelper.getConditionalDefines())
        .hasSize(8)
        .contains(
            "MSWINDOWS",
            "CPUX86",
            "DEBUG",
            "GGMSGDEBUGx",
            "LOGTOFILEx",
            "FullDebugMode",
            "RELEASE",
            "DefineFromSettings");
  }

  @Test
  public void testStandardLibraryPath() {
    DelphiProjectHelper delphiProjectHelper = new DelphiProjectHelper(settings, fs);

    assertThatThrownBy(delphiProjectHelper::standardLibraryPath)
        .isInstanceOf(RuntimeException.class);

    Path standardLibraryPath =
        DelphiUtils.getResource("/org/sonar/plugins/delphi/standardLibrary").toPath();
    when(settings.get(DelphiPlugin.STANDARD_LIBRARY_KEY))
        .thenReturn(Optional.of(standardLibraryPath.toAbsolutePath().toString()));

    assertThat(delphiProjectHelper.standardLibraryPath()).isEqualTo(standardLibraryPath);
  }

  @Test
  public void testSearchPathShouldSkipBlankPaths() {
    DelphiProjectHelper delphiProjectHelper = new DelphiProjectHelper(settings, fs);
    when(settings.getStringArray(DelphiPlugin.SEARCH_PATH_KEY))
        .thenReturn(new String[] {"", "\n", "\t\t\n"});

    assertThat(delphiProjectHelper.getSearchDirectories()).hasSize(1);
  }

  @Test
  public void testUnitAliases() {
    when(settings.getStringArray(DelphiPlugin.UNIT_ALIASES_KEY))
        .thenReturn(Arrays.array("Foo=Bar", "Blue=Red", "X=Y"));

    DelphiProjectHelper delphiProjectHelper = new DelphiProjectHelper(settings, fs);

    assertThat(delphiProjectHelper.getUnitAliases())
        .containsExactlyInAnyOrderEntriesOf(Map.of("Foo", "Bar", "Blue", "Red", "X", "Y"));
  }

  @Test
  public void testUnitAliasesShouldSkipBadSyntax() {
    when(settings.getStringArray(DelphiPlugin.UNIT_ALIASES_KEY))
        .thenReturn(Arrays.array("Foo=Bar", "BlueRed", "X==Y"));

    DelphiProjectHelper delphiProjectHelper = new DelphiProjectHelper(settings, fs);

    assertThat(delphiProjectHelper.getUnitAliases()).containsExactlyEntriesOf(Map.of("Foo", "Bar"));
  }
}
