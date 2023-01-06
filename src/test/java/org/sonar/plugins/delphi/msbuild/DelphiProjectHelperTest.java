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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.compiler.CompilerVersion;
import org.sonar.plugins.delphi.compiler.Toolchain;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.enviroment.EnvironmentVariableProvider;
import org.sonar.plugins.delphi.utils.DelphiUtils;

class DelphiProjectHelperTest {
  private static final String BDS_PATH =
      DelphiUtils.getResource("/org/sonar/plugins/delphi/bds").getAbsolutePath();
  private static final String STANDARD_LIBRARY_PATH =
      DelphiUtils.getResource("/org/sonar/plugins/delphi/bds/source").getAbsolutePath();
  private static final String PROJECTS_PATH = "/org/sonar/plugins/delphi/projects/";
  private static final File BASE_DIR = DelphiUtils.getResource(PROJECTS_PATH);
  private Configuration settings;
  private DefaultFileSystem fs;
  private EnvironmentVariableProvider environmentVariableProvider;

  @BeforeEach
  void setup() {
    settings = mock(Configuration.class);
    fs = new DefaultFileSystem(BASE_DIR);
    environmentVariableProvider = mock(EnvironmentVariableProvider.class);

    when(settings.get(DelphiPlugin.BDS_PATH_KEY)).thenReturn(Optional.of(BDS_PATH));
    when(environmentVariableProvider.getenv()).thenReturn(Collections.emptyMap());
    when(environmentVariableProvider.getenv(anyString())).thenReturn(null);

    String[] includes = {"BadSyntaxProject"};
    when(settings.getStringArray(DelphiPlugin.SEARCH_PATH_KEY)).thenReturn(includes);

    String[] defines = {"DefineFromSettings"};
    when(settings.getStringArray(DelphiPlugin.CONDITIONAL_DEFINES_KEY)).thenReturn(defines);
  }

  @Test
  void testInvalidIncludesShouldBeSkipped() {
    String[] includes = {"EmptyProject/empty", "BadSyntaxProject", "BadPath/Spooky"};
    when(settings.getStringArray(DelphiPlugin.SEARCH_PATH_KEY)).thenReturn(includes);

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getSearchDirectories()).hasSize(1);
  }

  @Test
  void testDprojProject() {
    InputFile inputFile =
        TestInputFileBuilder.create(
                DelphiLanguage.KEY,
                BASE_DIR,
                DelphiUtils.getResource(
                    PROJECTS_PATH + "SimpleProject/dproj/SimpleDelphiProject.dproj"))
            .build();
    fs.add(inputFile);

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getSearchDirectories()).hasSize(3);
    assertThat(delphiProjectHelper.getDebugSourceDirectories()).hasSize(1);

    assertThat(delphiProjectHelper.getConditionalDefines())
        .containsExactlyInAnyOrder(
            "ASSEMBLER",
            "DCC",
            "CPU386",
            "LOGTOFILEx",
            "DefineFromSettings",
            "CPUX86",
            "UNICODE",
            "DEBUG",
            "MSWINDOWS",
            "CPU32BITS",
            "NATIVECODE",
            "GGMSGDEBUGx",
            "CONSOLE",
            "WIN32",
            "UNDERSCOREIMPORTNAME",
            "CONDITIONALEXPRESSIONS",
            "FullDebugMode",
            "VER340");
    assertThat(delphiProjectHelper.getUnitScopeNames()).containsExactlyInAnyOrder("System", "Vcl");
  }

  @Test
  void testWorkgroupProject() {
    InputFile inputFile =
        TestInputFileBuilder.create(
                DelphiLanguage.KEY,
                BASE_DIR,
                DelphiUtils.getResource(PROJECTS_PATH + "SimpleProject/workgroup/All.groupproj"))
            .build();
    fs.add(inputFile);

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getSearchDirectories()).hasSize(3);
    assertThat(delphiProjectHelper.getConditionalDefines())
        .containsExactlyInAnyOrder(
            "ASSEMBLER",
            "DCC",
            "CPU386",
            "LOGTOFILEx",
            "DefineFromSettings",
            "CPUX86",
            "UNICODE",
            "DEBUG",
            "MSWINDOWS",
            "CPU32BITS",
            "NATIVECODE",
            "GGMSGDEBUGx",
            "CONSOLE",
            "WIN32",
            "UNDERSCOREIMPORTNAME",
            "CONDITIONALEXPRESSIONS",
            "FullDebugMode",
            "VER340");
    assertThat(delphiProjectHelper.getUnitScopeNames()).containsExactlyInAnyOrder("System", "Vcl");
  }

  @Test
  void testStandardLibraryPath() {
    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.standardLibraryPath()).isEqualTo(Path.of(STANDARD_LIBRARY_PATH));
  }

  @Test
  void testBdsPath() {
    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.bdsPath()).isEqualTo(Path.of(BDS_PATH));
  }

  @Test
  void testMissingBdsPathShouldThrowException() {
    when(settings.get(DelphiPlugin.BDS_PATH_KEY)).thenReturn(Optional.empty());

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThatThrownBy(delphiProjectHelper::bdsPath).isInstanceOf(RuntimeException.class);
  }

  @Test
  void testEnvironmentProjPath() {
    when(environmentVariableProvider.getenv("APPDATA")).thenReturn("/z/");

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.environmentProjPath())
        .isEqualTo(Path.of("/z/plugins/BDS/bds/environment.proj"));
  }

  @Test
  void testEnvironmentProjPathShouldBeNullWhenBDSPathIsTooShort() {
    when(settings.get(DelphiPlugin.BDS_PATH_KEY)).thenReturn(Optional.of("/a/short_path"));

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.environmentProjPath()).isNull();
  }

  @Test
  void testSearchPathShouldSkipBlankPaths() {
    when(settings.getStringArray(DelphiPlugin.SEARCH_PATH_KEY))
        .thenReturn(new String[] {"", "\n", "\t\t\n"});

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getSearchDirectories()).isEmpty();
  }

  @Test
  void testUnitAliases() {
    when(settings.getStringArray(DelphiPlugin.UNIT_ALIASES_KEY))
        .thenReturn(Arrays.array("Foo=Bar", "Blue=Red", "X=Y"));

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getUnitAliases())
        .containsExactlyInAnyOrderEntriesOf(Map.of("Foo", "Bar", "Blue", "Red", "X", "Y"));
  }

  @Test
  void testUnitAliasesShouldSkipBadSyntax() {
    when(settings.getStringArray(DelphiPlugin.UNIT_ALIASES_KEY))
        .thenReturn(Arrays.array("Foo=Bar", "BlueRed", "X==Y"));

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getUnitAliases()).containsExactlyEntriesOf(Map.of("Foo", "Bar"));
  }

  @Test
  void testToolchain() {
    when(settings.get(DelphiPlugin.COMPILER_TOOLCHAIN_KEY)).thenReturn(Optional.of("DCC64"));

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getToolchain()).isEqualTo(Toolchain.DCC64);
  }

  @Test
  void testDefaultToolchain() {
    when(settings.get(DelphiPlugin.COMPILER_TOOLCHAIN_KEY)).thenReturn(Optional.empty());

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getToolchain()).isEqualTo(Toolchain.DCC32);
  }

  @Test
  void testCompilerVersionDelphi1() {
    when(settings.get(DelphiPlugin.COMPILER_VERSION_KEY)).thenReturn(Optional.of("VER80"));

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getCompilerVersion())
        .isEqualTo(CompilerVersion.fromVersionNumber("8.0"));
  }

  @Test
  void testCompilerVersionDelphiXE6() {
    when(settings.get(DelphiPlugin.COMPILER_VERSION_KEY)).thenReturn(Optional.of("VER270"));

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getCompilerVersion())
        .isEqualTo(CompilerVersion.fromVersionNumber("27.0"));
  }

  @Test
  void testDefaultCompilerVersion() {
    when(settings.get(DelphiPlugin.COMPILER_VERSION_KEY)).thenReturn(Optional.empty());

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getCompilerVersion())
        .isEqualTo(DelphiPlugin.COMPILER_VERSION_DEFAULT);
  }

  @Test
  void testInvalidCompilerVersion() {
    when(settings.get(DelphiPlugin.COMPILER_VERSION_KEY)).thenReturn(Optional.of("INVALID"));

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getCompilerVersion())
        .isEqualTo(DelphiPlugin.COMPILER_VERSION_DEFAULT);
  }

  @Test
  void testNoFilesExist(@TempDir Path tempDir) {
    fs = new DefaultFileSystem(tempDir);

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.mainFiles()).isEmpty();
    assertThat(delphiProjectHelper.shouldExecuteOnProject()).isFalse();
  }

  @Test
  void testNoPasFilesExist(@TempDir Path tempDir) throws IOException {
    Files.createFile(tempDir.resolve("temp.txt"));
    fs = new DefaultFileSystem(tempDir);

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.mainFiles()).isEmpty();
    assertThat(delphiProjectHelper.shouldExecuteOnProject()).isFalse();
  }

  @Test
  void testPasFilesExist(@TempDir Path tempDir) throws IOException {
    Files.createFile(tempDir.resolve("temp.pas"));
    fs = new DefaultFileSystem(tempDir);

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.mainFiles()).isEmpty();
    assertThat(delphiProjectHelper.shouldExecuteOnProject()).isFalse();
  }
}
