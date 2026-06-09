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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.DelphiProperties;
import au.com.integradev.delphi.compiler.CompilerVersion;
import au.com.integradev.delphi.compiler.Toolchain;
import au.com.integradev.delphi.core.Delphi;
import au.com.integradev.delphi.enviroment.EnvironmentVariableProvider;
import au.com.integradev.delphi.utils.DelphiUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.config.Configuration;

class DelphiProjectHelperTest {
  private static final String PROJECTS_PATH = "/au/com/integradev/delphi/projects/";
  private static final String SOURCE_ENCODING_KEY = "sonar.sourceEncoding";
  private static final File BASE_DIR = DelphiUtils.getResource(PROJECTS_PATH);
  private Configuration settings;
  private DefaultFileSystem fs;
  private EnvironmentVariableProvider environmentVariableProvider;
  private String installationPath;
  private String standardLibraryPath;
  private String originalNativeEncoding;

  @BeforeEach
  void setup() throws IOException {
    originalNativeEncoding = System.getProperty("native.encoding");
    settings = mock(Configuration.class);
    fs = new DefaultFileSystem(BASE_DIR);
    environmentVariableProvider = mock(EnvironmentVariableProvider.class);

    installationPath =
        Files.createDirectories(Files.createTempDirectory("bds").resolve("foo/bds/bar"))
            .toAbsolutePath()
            .toString();
    standardLibraryPath = Path.of(installationPath, "source").toAbsolutePath().toString();

    when(settings.get(DelphiProperties.INSTALLATION_PATH_KEY))
        .thenReturn(Optional.of(installationPath));
    when(settings.get(SOURCE_ENCODING_KEY)).thenReturn(Optional.empty());
    when(environmentVariableProvider.getenv()).thenReturn(Collections.emptyMap());
    when(environmentVariableProvider.getenv(anyString())).thenReturn(null);

    String[] includes = {"BadSyntaxProject"};
    when(settings.getStringArray(DelphiProperties.SEARCH_PATH_KEY)).thenReturn(includes);

    String[] defines = {"DefineFromSettings"};
    when(settings.getStringArray(DelphiProperties.CONDITIONAL_DEFINES_KEY)).thenReturn(defines);
  }

  @AfterEach
  void restoreNativeEncoding() {
    if (originalNativeEncoding == null) {
      System.clearProperty("native.encoding");
    } else {
      System.setProperty("native.encoding", originalNativeEncoding);
    }
  }

  @Test
  void testInvalidIncludesShouldBeSkipped() {
    String[] includes = {"EmptyProject/empty", "BadSyntaxProject", "BadPath/Spooky"};
    when(settings.getStringArray(DelphiProperties.SEARCH_PATH_KEY)).thenReturn(includes);

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getSearchDirectories()).hasSize(1);
  }

  @Test
  void testDprojProject() {
    addInputFile(PROJECTS_PATH + "SimpleProject/dproj/SimpleDelphiProject.dproj");

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getSearchDirectories()).hasSize(4);
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
            "VER350");
    assertThat(delphiProjectHelper.getUnitScopeNames()).containsExactlyInAnyOrder("System", "Vcl");
  }

  @Test
  void testWorkgroupProject() {
    addInputFile(PROJECTS_PATH + "SimpleProject/workgroup/All.groupproj");

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getSearchDirectories()).hasSize(4);
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
            "VER350");
    assertThat(delphiProjectHelper.getUnitScopeNames()).containsExactlyInAnyOrder("System", "Vcl");
  }

  @Test
  void testStandardLibraryPath() {
    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.standardLibraryPath()).isEqualTo(Path.of(standardLibraryPath));
  }

  @Test
  void testInstallationPath() {
    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.installationPath()).isEqualTo(Path.of(installationPath));
  }

  @Test
  void testMissingInstallationPathShouldThrowException() {
    when(settings.get(DelphiProperties.INSTALLATION_PATH_KEY)).thenReturn(Optional.empty());

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThatThrownBy(delphiProjectHelper::installationPath).isInstanceOf(RuntimeException.class);
  }

  @Test
  void testEnvironmentProjPath() {
    when(environmentVariableProvider.getenv("APPDATA")).thenReturn("/z/");

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.environmentProjPath())
        .isEqualTo(Path.of("/z/foo/BDS/bar/environment.proj"));
  }

  @Test
  void testEnvironmentProjPathShouldBeNullWhenInstallationPathIsTooShort() {
    when(settings.get(DelphiProperties.INSTALLATION_PATH_KEY))
        .thenReturn(Optional.of("/a/short_path"));

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.environmentProjPath()).isNull();
  }

  @Test
  void testSearchPathShouldSkipBlankPaths() {
    when(settings.getStringArray(DelphiProperties.SEARCH_PATH_KEY))
        .thenReturn(new String[] {"", "\n", "\t\t\n"});

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getSearchDirectories()).isEmpty();
  }

  @Test
  void testUnitAliases() {
    when(settings.getStringArray(DelphiProperties.UNIT_ALIASES_KEY))
        .thenReturn(Arrays.array("Foo=Bar", "Blue=Red", "X=Y"));

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getUnitAliases())
        .containsExactlyInAnyOrderEntriesOf(Map.of("Foo", "Bar", "Blue", "Red", "X", "Y"));
  }

  @Test
  void testUnitAliasesShouldSkipBadSyntax() {
    when(settings.getStringArray(DelphiProperties.UNIT_ALIASES_KEY))
        .thenReturn(Arrays.array("Foo=Bar", "BlueRed", "X==Y"));

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getUnitAliases()).containsExactlyEntriesOf(Map.of("Foo", "Bar"));
  }

  @Test
  void testToolchain() {
    when(settings.get(DelphiProperties.COMPILER_TOOLCHAIN_KEY)).thenReturn(Optional.of("DCC64"));

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getToolchain()).isEqualTo(Toolchain.DCC64);
  }

  @Test
  void testDefaultToolchain() {
    when(settings.get(DelphiProperties.COMPILER_TOOLCHAIN_KEY)).thenReturn(Optional.empty());

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getToolchain()).isEqualTo(Toolchain.DCC32);
  }

  @Test
  void testCompilerVersionDelphi1() {
    when(settings.get(DelphiProperties.COMPILER_VERSION_KEY)).thenReturn(Optional.of("VER80"));

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getCompilerVersion())
        .isEqualTo(CompilerVersion.fromVersionNumber("8.0"));
  }

  @Test
  void testCompilerVersionDelphiXE6() {
    when(settings.get(DelphiProperties.COMPILER_VERSION_KEY)).thenReturn(Optional.of("VER270"));

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getCompilerVersion())
        .isEqualTo(CompilerVersion.fromVersionNumber("27.0"));
  }

  @Test
  void testDefaultCompilerVersion() {
    when(settings.get(DelphiProperties.COMPILER_VERSION_KEY)).thenReturn(Optional.empty());

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getCompilerVersion())
        .isEqualTo(DelphiProperties.COMPILER_VERSION_DEFAULT);
  }

  @Test
  void testInvalidCompilerVersion() {
    when(settings.get(DelphiProperties.COMPILER_VERSION_KEY)).thenReturn(Optional.of("INVALID"));

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getCompilerVersion())
        .isEqualTo(DelphiProperties.COMPILER_VERSION_DEFAULT);
  }

  @Test
  void testProjectCodePageProvidesAnsiCharset() {
    addInputFile(PROJECTS_PATH + "CodePageProject/Utf8.dproj");

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getAnsiCharset()).isEqualTo(StandardCharsets.UTF_8);
  }

  @Test
  void testConfiguredCodePageOverridesProjectCodePage() {
    addInputFile(PROJECTS_PATH + "CodePageProject/Utf8.dproj");
    when(settings.get(DelphiProperties.CODE_PAGE_KEY)).thenReturn(Optional.of("1251"));

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getAnsiCharset().name()).isEqualTo("windows-1251");
  }

  @Test
  void testConfiguredAcpCodePageUsesNativeCharset() {
    setNativeEncoding(StandardCharsets.ISO_8859_1.name());
    when(settings.get(DelphiProperties.CODE_PAGE_KEY)).thenReturn(Optional.of("0"));

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getAnsiCharset()).isEqualTo(StandardCharsets.ISO_8859_1);
  }

  @Test
  void testInvalidConfiguredCodePageFallsBackToProjectCodePage() {
    addInputFile(PROJECTS_PATH + "CodePageProject/Utf8.dproj");
    when(settings.get(DelphiProperties.CODE_PAGE_KEY)).thenReturn(Optional.of("not-a-number"));

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getAnsiCharset()).isEqualTo(StandardCharsets.UTF_8);
  }

  @Test
  void testUnsupportedConfiguredCodePageFallsBackToNativeCharset() {
    setNativeEncoding(StandardCharsets.ISO_8859_1.name());

    when(settings.get(DelphiProperties.CODE_PAGE_KEY)).thenReturn(Optional.of("99999"));

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getAnsiCharset()).isEqualTo(StandardCharsets.ISO_8859_1);
  }

  @Test
  void testNoConfiguredOrProjectCodePagesFallBackToNativeCharset() {
    setNativeEncoding(StandardCharsets.ISO_8859_1.name());

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getAnsiCharset()).isEqualTo(StandardCharsets.ISO_8859_1);
  }

  @Test
  void testConflictingProjectCodePagesFallBackToNativeCharset() {
    setNativeEncoding(StandardCharsets.ISO_8859_1.name());

    addInputFile(PROJECTS_PATH + "CodePageProject/Utf8.dproj");
    addInputFile(PROJECTS_PATH + "CodePageProject/Windows1252.dproj");

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getAnsiCharset()).isEqualTo(StandardCharsets.ISO_8859_1);
  }

  @Test
  void testMissingProjectCodePageFallsBackToNativeCharset() {
    setNativeEncoding("windows-1252");

    addInputFile(PROJECTS_PATH + "CodePageProject/Utf8.dproj");
    addInputFile(PROJECTS_PATH + "SimpleProject/dproj/SimpleDelphiProject.dproj");

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getAnsiCharset()).isEqualTo(Charset.forName("windows-1252"));
  }

  @Test
  void testBlankProjectCodePageFallsBackToNativeCharset() {
    setNativeEncoding("windows-1252");

    addInputFile(PROJECTS_PATH + "CodePageProject/Utf8.dproj");
    addInputFile(PROJECTS_PATH + "CodePageProject/Blank.dproj");

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getAnsiCharset()).isEqualTo(Charset.forName("windows-1252"));
  }

  @Test
  void testAcpProjectCodePageUsesNativeCharsetWithoutConflictWhenItMatchesExplicitCodePage() {
    setNativeEncoding("windows-1252");

    addInputFile(PROJECTS_PATH + "CodePageProject/Acp.dproj");
    addInputFile(PROJECTS_PATH + "CodePageProject/Windows1252.dproj");

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getAnsiCharset()).isEqualTo(Charset.forName("windows-1252"));
  }

  @Test
  void testConfiguredSourceEncodingProvidesCharset() {
    fs.setEncoding(StandardCharsets.UTF_16LE);
    when(settings.get(SOURCE_ENCODING_KEY)).thenReturn(Optional.of("UTF-16LE"));

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getCharset()).isEqualTo(StandardCharsets.UTF_16LE);
  }

  @Test
  void testMissingSourceEncodingFallsBackToAnsiCharset() {
    addInputFile(PROJECTS_PATH + "CodePageProject/Windows1252.dproj");
    fs.setEncoding(StandardCharsets.UTF_8);

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.getCharset().name()).isEqualTo("windows-1252");
  }

  @Test
  void testNoFilesExist(@TempDir Path tempDir) {
    fs = new DefaultFileSystem(tempDir);

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.inputFiles()).isEmpty();
  }

  @Test
  void testNoPasFilesExist(@TempDir Path tempDir) throws IOException {
    Files.createFile(tempDir.resolve("temp.txt"));
    fs = new DefaultFileSystem(tempDir);

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.inputFiles()).isEmpty();
  }

  @Test
  void testPasFilesExist(@TempDir Path tempDir) throws IOException {
    Files.createFile(tempDir.resolve("temp.pas"));
    fs = new DefaultFileSystem(tempDir);

    DelphiProjectHelper delphiProjectHelper =
        new DelphiProjectHelper(settings, fs, environmentVariableProvider);

    assertThat(delphiProjectHelper.inputFiles()).isEmpty();
  }

  private void addInputFile(String resourcePath) {
    InputFile inputFile =
        TestInputFileBuilder.create(Delphi.KEY, BASE_DIR, DelphiUtils.getResource(resourcePath))
            .build();
    fs.add(inputFile);
  }

  private static void setNativeEncoding(String encoding) {
    System.setProperty("native.encoding", encoding);
  }
}
