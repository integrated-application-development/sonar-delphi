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
package org.sonar.plugins.delphi.symbol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.sonar.plugins.delphi.file.DelphiFile;
import org.sonar.plugins.delphi.preprocessor.search.SearchPath;
import org.sonar.plugins.delphi.symbol.SymbolTableBuilder.SymbolTableConstructionException;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.delphi.utils.builders.DelphiTestUnitBuilder;
import org.sonar.plugins.delphi.utils.types.TypeFactoryUtils;

class SymbolTableBuilderTest {
  private static final Path STANDARD_LIBRARY =
      DelphiUtils.getResource("/org/sonar/plugins/delphi/bds/source").toPath();

  @Test
  void testNonexistentStandardLibraryPath(@TempDir Path tempDir) {
    SymbolTableBuilder builder =
        SymbolTable.builder()
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
  void testBuildWithoutTypeFactory() {
    SymbolTableBuilder builder = SymbolTable.builder();
    assertThatThrownBy(builder::build).isInstanceOf(SymbolTableConstructionException.class);
  }

  @Test
  void testEmptyStandardLibrary(@TempDir Path standardLibraryPath) {
    SymbolTableBuilder builder =
        SymbolTable.builder()
            .typeFactory(TypeFactoryUtils.defaultFactory())
            .standardLibraryPath(standardLibraryPath);

    assertThatThrownBy(builder::build).isInstanceOf(SymbolTableConstructionException.class);
  }

  @Test
  void testStandardLibraryWithInvalidSystemUnit(@TempDir Path standardLibraryPath)
      throws IOException {
    DelphiFile sysInit = new DelphiTestUnitBuilder().unitName("SysInit").delphiFile();
    Files.move(sysInit.getSourceCodeFile().toPath(), standardLibraryPath.resolve("SysInit.pas"));

    DelphiFile system = new DelphiTestUnitBuilder().unitName("System").delphiFile();
    Files.move(system.getSourceCodeFile().toPath(), standardLibraryPath.resolve("System.pas"));

    SymbolTableBuilder builder =
        SymbolTable.builder()
            .typeFactory(TypeFactoryUtils.defaultFactory())
            .standardLibraryPath(standardLibraryPath);

    assertThatThrownBy(builder::build).isInstanceOf(SymbolTableConstructionException.class);
  }

  @Test
  void testStandardLibrarySearchPathShouldExcludeToolsUnits() {
    DelphiFile sourceFile =
        new DelphiTestUnitBuilder()
            .appendDecl("uses")
            .appendDecl("  ShouldBeExcludedFromSearchPath;")
            .delphiFile();

    SymbolTable symbolTable =
        SymbolTable.builder()
            .typeFactory(TypeFactoryUtils.defaultFactory())
            .standardLibraryPath(STANDARD_LIBRARY)
            .sourceFiles(List.of(sourceFile.getSourceCodeFile().toPath()))
            .build();

    assertThat(
            symbolTable.getUnitByPath(
                STANDARD_LIBRARY.resolve("Tools/ShouldBeExcludedFromSearchPath.pas").toString()))
        .isNull();
  }
}
