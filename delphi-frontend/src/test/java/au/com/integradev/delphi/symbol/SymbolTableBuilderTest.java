/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package au.com.integradev.delphi.symbol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.preprocessor.DelphiPreprocessorFactory;
import au.com.integradev.delphi.preprocessor.search.SearchPath;
import au.com.integradev.delphi.symbol.SymbolTableBuilder.SymbolTableConstructionException;
import au.com.integradev.delphi.utils.DelphiUtils;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentMatchers;

class SymbolTableBuilderTest {
  private static final Path STANDARD_LIBRARY =
      DelphiUtils.getResource("/au/com/integradev/delphi/bds/source").toPath();

  @Test
  void testNonexistentStandardLibraryPath(@TempDir Path tempDir) {
    SymbolTableBuilder builder =
        SymbolTable.builder()
            .preprocessorFactory(new DelphiPreprocessorFactory(Platform.WINDOWS))
            .typeFactory(TypeFactoryUtils.defaultFactory())
            .standardLibraryPath(tempDir.resolve("nonexistent"));
    assertThatThrownBy(builder::build).isInstanceOf(SymbolTableConstructionException.class);
  }

  @Test
  void testIOExceptionWhileProcessingSearchPath() throws IOException {
    SymbolTableBuilder builder = SymbolTable.builder();

    FileSystemProvider provider = mock(FileSystemProvider.class);
    when(provider.readAttributes(any(), ArgumentMatchers.<Class<BasicFileAttributes>>any(), any()))
        .thenThrow(IOException.class);

    FileSystem fileSystem = mock(FileSystem.class);
    when(fileSystem.provider()).thenReturn(provider);

    Path path = mock(Path.class);
    when(path.getFileSystem()).thenReturn(fileSystem);

    SearchPath searchPath = mock(SearchPath.class);
    when(searchPath.getRootDirectories()).thenReturn(Set.of(path));

    builder.searchPath(searchPath);

    assertThatThrownBy(builder::build).isInstanceOf(SymbolTableConstructionException.class);
  }

  @Test
  void testBuildWithoutPreprocessorFactory() {
    SymbolTableBuilder builder = SymbolTable.builder();
    assertThatThrownBy(builder::build).isInstanceOf(SymbolTableConstructionException.class);
  }

  @Test
  void testBuildWithoutTypeFactory() {
    SymbolTableBuilder builder =
        SymbolTable.builder().preprocessorFactory(new DelphiPreprocessorFactory(Platform.WINDOWS));
    assertThatThrownBy(builder::build).isInstanceOf(SymbolTableConstructionException.class);
  }

  @Test
  void testEmptyStandardLibrary(@TempDir Path standardLibraryPath) {
    SymbolTableBuilder builder =
        SymbolTable.builder()
            .preprocessorFactory(new DelphiPreprocessorFactory(Platform.WINDOWS))
            .typeFactory(TypeFactoryUtils.defaultFactory())
            .standardLibraryPath(standardLibraryPath);

    assertThatThrownBy(builder::build).isInstanceOf(SymbolTableConstructionException.class);
  }

  @Test
  void testStandardLibraryWithInvalidSystemUnit(@TempDir Path standardLibraryPath)
      throws IOException {
    Files.writeString(
        standardLibraryPath.resolve("SysInit.pas"),
        "unit SysInit;\ninterface\nimplementation\nend.");

    Files.writeString(
        standardLibraryPath.resolve("System.pas"), "unit System;\ninterface\nimplementation\nend.");

    SymbolTableBuilder builder =
        SymbolTable.builder()
            .preprocessorFactory(new DelphiPreprocessorFactory(Platform.WINDOWS))
            .typeFactory(TypeFactoryUtils.defaultFactory())
            .standardLibraryPath(standardLibraryPath);

    assertThatThrownBy(builder::build).isInstanceOf(SymbolTableConstructionException.class);
  }

  @Test
  void testStandardLibrarySearchPathShouldExcludeToolsUnits(@TempDir Path tempDir)
      throws IOException {
    Path sourceFilePath = tempDir.resolve("SourceFile.pas");
    Files.writeString(
        sourceFilePath,
        "unit SourceFile;\n"
            + "interface\n"
            + "uses\n"
            + "  ShouldBeExcludedFromSearchPath;"
            + "implementation\n"
            + "end.");

    SymbolTable symbolTable =
        SymbolTable.builder()
            .preprocessorFactory(new DelphiPreprocessorFactory(Platform.WINDOWS))
            .typeFactory(TypeFactoryUtils.defaultFactory())
            .standardLibraryPath(STANDARD_LIBRARY)
            .sourceFiles(List.of(sourceFilePath))
            .build();

    assertThat(
            symbolTable.getUnitByPath(
                STANDARD_LIBRARY.resolve("Tools/ShouldBeExcludedFromSearchPath.pas").toString()))
        .isNull();
  }
}
