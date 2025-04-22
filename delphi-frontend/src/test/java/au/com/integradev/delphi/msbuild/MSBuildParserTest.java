/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.enviroment.EnvironmentVariableProvider;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MSBuildParserTest {
  private EnvironmentVariableProvider environmentVariableProvider;

  @TempDir private Path tempDir;

  @BeforeEach
  void setUp() {
    environmentVariableProvider = mock(EnvironmentVariableProvider.class);
    when(environmentVariableProvider.getenv()).thenReturn(Collections.emptyMap());
    when(environmentVariableProvider.getenv(any())).thenReturn("");
  }

  Path createFile(String filename, String... lines) {
    try {
      var path = tempDir.resolve(filename);
      Files.write(path, List.of(lines), StandardCharsets.UTF_8);
      return path;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Test
  void testParseProperties() {
    var file =
        createFile(
            "properties.proj",
            "<Project>",
            "  <PropertyGroup>",
            "    <FooProp>Blomp</FooProp>",
            "    <BarProp>Blimp$(FooProp)</BarProp>",
            "  </PropertyGroup>",
            "</Project>");
    var state = new MSBuildParser(file, environmentVariableProvider).parse();

    assertThat(state.getProperties())
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "FooProp", "Blomp",
                "BarProp", "BlimpBlomp"));
  }

  @Test
  void testParseItems() {
    var file =
        createFile(
            "items.proj",
            "<Project>",
            "  <PropertyGroup>",
            "    <FooProp>Blomp</FooProp>",
            "    <BarProp>Blimp$(FooProp)</BarProp>",
            "  </PropertyGroup>",
            "  <ItemGroup>",
            "    <BazItem Include=\"Alpha;Beta;Gamma\">",
            "      <MyMeta>$(BarProp)</MyMeta>",
            "      <MyOtherMeta>Hello world!</MyOtherMeta>",
            "    </BazItem>",
            "  </ItemGroup>",
            "</Project>");
    var state = new MSBuildParser(file, environmentVariableProvider).parse();

    assertThat(state.getItems("BazItem")).hasSize(3);
    var item = state.getItems("BazItem").get(1);
    assertThat(item.getIdentity()).isEqualTo("Beta");
    assertThat(item.getMetadata("MyMeta")).isEqualTo("BlimpBlomp");
    assertThat(item.getMetadata("MyOtherMeta")).isEqualTo("Hello world!");
  }

  @Test
  void testParseSelfReferentialItems() {
    var file =
        createFile(
            "selfReferentialItems.proj",
            "<Project>",
            "  <ItemGroup>",
            "    <BazItem Include=\"Alpha;Beta;Gamma\">",
            "      <SignOff>From %(Identity)</SignOff>",
            "      <Greeting>Hello %(SignOff)!</Greeting>",
            "    </BazItem>",
            "  </ItemGroup>",
            "</Project>");
    var state = new MSBuildParser(file, environmentVariableProvider).parse();

    assertThat(state.getItems("BazItem")).hasSize(3);
    var item = state.getItems("BazItem").get(1);
    assertThat(item.getIdentity()).isEqualTo("Beta");
    assertThat(item.getMetadata("SignOff")).isEqualTo("From Beta");
    assertThat(item.getMetadata("Greeting")).isEqualTo("Hello From Beta!");
  }

  @Test
  void testImportProperties() {
    createFile(
        "imported.proj",
        "<Project>",
        "  <PropertyGroup>",
        "    <ChangedProp>$(ChangedProp) - changed by imported file</ChangedProp>",
        "    <ImportedProp>Hello from imported file!</ImportedProp>",
        "  </PropertyGroup>",
        "</Project>");

    var mainFile =
        createFile(
            "importingProject.proj",
            "<Project>",
            "  <PropertyGroup>",
            "    <ChangedProp>populated in main file</ChangedProp>",
            "    <UnchangedProp>populated in main file - never changed</UnchangedProp>",
            "  </PropertyGroup>",
            "  <Import Project=\"imported.proj\"/>",
            "  <PropertyGroup>",
            "    <ChangedProp>$(ChangedProp) - changed again by main file</ChangedProp>",
            "  </PropertyGroup>",
            "</Project>");

    var state = new MSBuildParser(mainFile, environmentVariableProvider).parse();
    assertThat(state.getProperties())
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "ChangedProp",
                "populated in main file - changed by imported file - changed again by main file",
                "UnchangedProp",
                "populated in main file - never changed",
                "ImportedProp",
                "Hello from imported file!"));
  }

  @Test
  void testParseComplexExpressions() {
    var file =
        createFile(
            "complexExpressions.proj",
            "<Project>",
            "  <PropertyGroup>",
            "    <FooProp>there</FooProp>",
            "  </PropertyGroup>",
            "  <ItemGroup>",
            "    <BazItem Include=\"Alpha;Beta;Gamma\">",
            "      <MyMeta>$(FooProp) %(Identity)</MyMeta>",
            "      <MyOtherMeta>Hello %(MyMeta)!</MyOtherMeta>",
            "    </BazItem>",
            "  </ItemGroup>",
            "  <PropertyGroup>",
            "    <Result>@(BazItem->'%(MyOtherMeta)', ':') @(BazItem)</Result>",
            "  </PropertyGroup>",
            "</Project>");
    var state = new MSBuildParser(file, environmentVariableProvider).parse();

    assertThat(state.getProperty("Result"))
        .isEqualTo("Hello there Alpha!:Hello there Beta!:Hello there Gamma! Alpha;Beta;Gamma");
  }

  @Test
  void testParseWellKnownProperties() {
    var file =
        createFile(
            "wellKnownProperties.proj",
            "<Project>",
            "  <PropertyGroup>",
            "    <FooProp>Blomp$(MSBuildProjectDirectory)</FooProp>",
            "    <BarProp>Blimp$(OS)</BarProp>",
            "  </PropertyGroup>",
            "</Project>");
    var state = new MSBuildParser(file, environmentVariableProvider).parse();

    assertThat(state.getProperties())
        .containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "FooProp",
                String.format("Blomp%s%s", tempDir, FileSystems.getDefault().getSeparator()),
                "BarProp",
                "BlimpWindows_NT"));
  }

  @Test
  void testParseThisFileWellKnownProperties() {
    createFile(
        "wellKnownImportedFile.proj",
        "<Project>",
        "  <PropertyGroup>",
        "    <ProjectMsg>$(ProjectMsg), imported file project is"
            + " $(MSBuildProjectName)</ProjectMsg>",
        "    <ThisFileMsg>$(ThisFileMsg), imported file is $(MSBuildThisFileName)</ThisFileMsg>",
        "  </PropertyGroup>",
        "</Project>");

    var mainFile =
        createFile(
            "wellKnownMainFile.proj",
            "<Project>",
            "  <PropertyGroup>",
            "    <ProjectMsg>Main file project is $(MSBuildProjectName)</ProjectMsg>",
            "    <ThisFileMsg>Main file is $(MSBuildThisFileName)</ThisFileMsg>",
            "  </PropertyGroup>",
            "  <Import Project=\"wellKnownImportedFile.proj\"/>",
            "</Project>");

    var state = new MSBuildParser(mainFile, environmentVariableProvider).parse();
    assertThat(state.getProperty("ProjectMsg"))
        .isEqualTo(
            "Main file project is wellKnownMainFile, imported file project is wellKnownMainFile");
    assertThat(state.getProperty("ThisFileMsg"))
        .isEqualTo("Main file is wellKnownMainFile, imported file is wellKnownImportedFile");
  }

  @Test
  void testParseNonexistentImport() {
    var mainFile =
        createFile(
            "fileWithNonexistentImport.proj",
            "<Project>",
            "  <Import Project=\"nonexistent.proj\"/>",
            "</Project>");

    var parser = new MSBuildParser(mainFile, environmentVariableProvider);
    assertThatNoException().isThrownBy(parser::parse);
  }
}
