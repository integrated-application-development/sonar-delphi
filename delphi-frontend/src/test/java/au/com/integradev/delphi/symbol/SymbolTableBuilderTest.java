/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.preprocessor.DelphiPreprocessorFactory;
import au.com.integradev.delphi.preprocessor.search.SearchPath;
import au.com.integradev.delphi.symbol.SymbolTableBuilder.SymbolTableConstructionException;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;

class SymbolTableBuilderTest {
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
  void testStandardLibrarySearchPathShouldExcludeToolsUnits(
      @TempDir Path standardLibraryPath, @TempDir Path tempDir) throws IOException {
    createStandardLibrary(standardLibraryPath);

    Path toolsPath = standardLibraryPath.resolve("Tools");
    Files.createDirectories(toolsPath);

    Path excludedPath = toolsPath.resolve("ShouldBeExcludedFromSearchPath.pas");
    Files.writeString(
        excludedPath,
        "unit ShouldBeExcludedFromSearchPath;\n" //
            + "interface\n"
            + "implementation\n"
            + "end.");

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
            .standardLibraryPath(standardLibraryPath)
            .sourceFiles(List.of(sourceFilePath))
            .build();

    assertThat(symbolTable.getUnitByPath(excludedPath.toString())).isNull();
  }

  @Test
  void testReferencedFilesArePrioritizedOverSearchPath(
      @TempDir Path standardLibraryPath,
      @TempDir Path searchPathRoot,
      @TempDir Path referencedFilesPath)
      throws IOException {
    createStandardLibrary(standardLibraryPath);
    createStandardLibrary(searchPathRoot);
    createStandardLibrary(referencedFilesPath);

    List<Path> referencedFiles;
    try (Stream<Path> referencedFilesStream = Files.list(referencedFilesPath)) {
      referencedFiles = referencedFilesStream.collect(Collectors.toUnmodifiableList());
    }

    SymbolTable symbolTable =
        SymbolTable.builder()
            .preprocessorFactory(new DelphiPreprocessorFactory(Platform.WINDOWS))
            .typeFactory(TypeFactoryUtils.defaultFactory())
            .standardLibraryPath(standardLibraryPath)
            .searchPath(SearchPath.create(List.of(searchPathRoot)))
            .referencedFiles(referencedFiles)
            .build();

    assertThat(symbolTable.getUnitByPath(standardLibraryPath.resolve("SysInit.pas").toString()))
        .isNull();
    assertThat(symbolTable.getUnitByPath(searchPathRoot.resolve("SysInit.pas").toString()))
        .isNull();
    assertThat(symbolTable.getUnitByPath(referencedFilesPath.resolve("SysInit.pas").toString()))
        .isNotNull();
  }

  @Test
  void testSearchPathIsPrioritizedOverStandardLibrary(
      @TempDir Path standardLibraryPath, @TempDir Path searchPathRoot) throws IOException {
    createStandardLibrary(standardLibraryPath);
    createStandardLibrary(searchPathRoot);

    SymbolTable symbolTable =
        SymbolTable.builder()
            .preprocessorFactory(new DelphiPreprocessorFactory(Platform.WINDOWS))
            .typeFactory(TypeFactoryUtils.defaultFactory())
            .standardLibraryPath(standardLibraryPath)
            .searchPath(SearchPath.create(List.of(searchPathRoot)))
            .build();

    assertThat(symbolTable.getUnitByPath(standardLibraryPath.resolve("SysInit.pas").toString()))
        .isNull();
    assertThat(symbolTable.getUnitByPath(searchPathRoot.resolve("SysInit.pas").toString()))
        .isNotNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {"#$@", "unit ErrorUnit;"})
  void testRecognitionErrorInImportShouldNotThrow(
      String errorSource, @TempDir Path standardLibraryPath, @TempDir Path tempDir)
      throws IOException {
    createStandardLibrary(standardLibraryPath);

    Path includePath = tempDir.resolve("include");
    Files.createDirectories(includePath);

    Path errorUnitPath = includePath.resolve("ErrorUnit.pas");
    Files.writeString(errorUnitPath, errorSource);

    Path sourceFilePath = tempDir.resolve("SourceFile.pas");
    Files.writeString(
        sourceFilePath,
        "unit SourceFile;\n"
            + "interface\n"
            + "uses\n"
            + "  ErrorUnit;"
            + "implementation\n"
            + "end.");

    SymbolTableBuilder symbolTable =
        SymbolTable.builder()
            .preprocessorFactory(new DelphiPreprocessorFactory(Platform.WINDOWS))
            .typeFactory(TypeFactoryUtils.defaultFactory())
            .standardLibraryPath(standardLibraryPath)
            .sourceFiles(List.of(sourceFilePath))
            .searchPath(SearchPath.create(List.of(includePath)));

    assertThatCode(symbolTable::build).doesNotThrowAnyException();
  }

  private static void createStandardLibrary(Path path) throws IOException {
    Files.writeString(
        path.resolve("SysInit.pas"),
        "unit SysInit;\n" //
            + "interface\n"
            + "implementation\n"
            + "end.");

    Files.writeString(
        path.resolve("System.pas"),
        "unit System;\n"
            + "interface\n"
            + "type\n"
            + "  TObject = class\n"
            + "  end;\n"
            + "  IInterface = interface\n"
            + "  end;\n"
            + "  TClassHelperBase = class\n"
            + "  end;\n"
            + "  TVarRec = record\n"
            + "  end;\n"
            + "implementation\n"
            + "end.");
  }
}
