/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package au.com.integradev.delphi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.core.Delphi;
import au.com.integradev.delphi.executor.DelphiMasterExecutor;
import au.com.integradev.delphi.msbuild.DelphiProjectHelper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

class DelphiSensorTest {
  private final DelphiMasterExecutor executor = mock(DelphiMasterExecutor.class);
  private final DelphiProjectHelper delphiProjectHelper = mock(DelphiProjectHelper.class);
  private Path baseDir;

  private DelphiSensor sensor;

  @BeforeEach
  void setup() throws IOException {
    baseDir = Files.createTempDirectory("baseDir");

    sensor = new DelphiSensor(delphiProjectHelper, executor);
    when(delphiProjectHelper.shouldExecuteOnProject()).thenReturn(true);
    when(delphiProjectHelper.getToolchain())
        .thenReturn(DelphiProperties.COMPILER_TOOLCHAIN_DEFAULT);
    when(delphiProjectHelper.getCompilerVersion())
        .thenReturn(DelphiProperties.COMPILER_VERSION_DEFAULT);

    Path standardLibraryPath = Files.createDirectories(baseDir.resolve("bds/source"));

    Files.writeString(
        standardLibraryPath.resolve("SysInit.pas"),
        "unit SysInit;\ninterface\nimplementation\nend.");

    Files.writeString(
        standardLibraryPath.resolve("System.pas"),
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

    setupFile("unit SourceFile;\ninterface\nimplementation\nend.");
    when(delphiProjectHelper.standardLibraryPath()).thenReturn(standardLibraryPath);
  }

  private void setupFile(String content) {
    Path sourceFilePath = baseDir.resolve("SourceFile.pas");

    try {
      Files.writeString(sourceFilePath, content);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    InputFile inputFile =
        TestInputFileBuilder.create("moduleKey", baseDir.toFile(), sourceFilePath.toFile())
            .setContents(content)
            .setLanguage(Delphi.KEY)
            .setType(InputFile.Type.MAIN)
            .build();

    when(delphiProjectHelper.inputFiles()).thenReturn(List.of(inputFile));
    when(delphiProjectHelper.getFile(anyString())).thenReturn(inputFile);
  }

  private void assertParsingErrorWithMessage(int expectedLine, String expectedMessage) {
    assertParsingErrorMatchingMessage(expectedLine, Pattern.quote(expectedMessage));
  }

  private void assertParsingErrorMatchingMessage(int expectedLine, String expectedRegex) {
    SensorContextTester context = SensorContextTester.create(baseDir);

    sensor.execute(context);

    assertThat(context.allIssues())
        .hasSize(1)
        .element(0)
        .satisfies(
            issue -> {
              assertThat(issue.ruleKey().repository()).isEqualTo("community-delphi");
              assertThat(issue.ruleKey().rule()).isEqualTo("ParsingError");
              assertThat(issue.primaryLocation().message()).matches(expectedRegex);

              TextRange position = issue.primaryLocation().textRange();
              if (expectedLine == 0) {
                assertThat(position).isNull();
              } else {
                assertThat(position).isNotNull();
                assertThat(position.start().line()).isEqualTo(expectedLine);
                assertThat(position.end().line()).isEqualTo(expectedLine);
              }
            });
  }

  @AfterEach
  void teardown() {
    FileUtils.deleteQuietly(baseDir.toFile());
  }

  @Test
  void testToString() {
    final String toString = sensor.toString();
    assertThat(toString).isEqualTo("DelphiSensor");
  }

  @Test
  void testDescribe() {
    final SensorDescriptor mockDescriptor = mock(SensorDescriptor.class);
    when(mockDescriptor.onlyOnLanguage(anyString())).thenReturn(mockDescriptor);

    sensor.describe(mockDescriptor);

    verify(mockDescriptor).onlyOnLanguage(Delphi.KEY);
    verify(mockDescriptor).name("DelphiSensor");
  }

  @Test
  void testSensorShouldAllowExceptionsToPropagate() {
    final RuntimeException expectedException = new RuntimeException();

    willThrow(expectedException).given(executor).setup();

    assertThatThrownBy(() -> sensor.execute(mock())).isEqualTo(expectedException);
  }

  @Test
  void testWhenShouldExecuteOnProjectReturnsFalseThenExecutorIsNotCalled() {
    when(delphiProjectHelper.shouldExecuteOnProject()).thenReturn(false);

    sensor.execute(mock());

    verify(executor, never()).execute(any(), any());
  }

  @Test
  void testWhenShouldExecuteOnProjectReturnsTrueThenExecutorIsCalled() {
    when(delphiProjectHelper.shouldExecuteOnProject()).thenReturn(true);

    sensor.execute(mock());

    verify(executor, times(1)).execute(any(), any());
  }

  @Test
  void testFileWithLexerErrorRaisesParsingErrorIssue() {
    setupFile("\n\n'unterminated string literal");
    assertParsingErrorWithMessage(
        3, "Parse error (line 3:28 mismatched character '<EOF>' expecting ''')");
  }

  @Test
  void testFileWithParserErrorRaisesParsingErrorIssue() {
    setupFile("\n\n\n\n;");
    assertParsingErrorWithMessage(5, "Parse error (line 5:0 no viable alternative at input ';')");
  }

  @Test
  void testFileWithCompilerDirectiveExpressionLexerErrorRaisesParsingErrorIssue() {
    setupFile("\n\n{$if 1..2}");
    assertParsingErrorWithMessage(3, "Parse error (line 3:0 Unexpected '.' in numeric literal)");
  }

  @Test
  void testFileWithCompilerDirectiveExpressionParserErrorRaisesParsingErrorIssue() {
    setupFile("\n\n\n\n{$elseif}");
    assertParsingErrorWithMessage(
        5, "Parse error (line 5:0 Expected expression. Got '<end of input>')");
  }

  @Test
  void testEmptyFileRaisesParsingErrorIssue() {
    setupFile("");
    assertParsingErrorWithMessage(0, "Parse error (Empty files are not allowed)");
  }

  @Test
  void testIncludeFileWithLexerErrorRaisesParsingErrorIssue() throws IOException {
    setupFile("\n{$I include.inc}");
    Files.writeString(baseDir.resolve("include.inc"), "\n\n'unterminated string literal");
    assertParsingErrorWithMessage(
        2,
        "Parse error (included on line 2 :: line 3:28 mismatched character '<EOF>' expecting ''')");
  }

  @Test
  void testIncludeFileWithParserErrorRaisesParsingErrorIssue() throws IOException {
    setupFile("\n\n{$INCLUDE include.inc}");
    Files.writeString(baseDir.resolve("include.inc"), "\n\n\n\n;");
    assertParsingErrorWithMessage(
        3, "Parse error (included on line 3 :: line 5:0 no viable alternative at input ';')");
  }

  @Test
  void testSelfReferencingIncludeRaisesParsingErrorIssue() throws IOException {
    setupFile("\n\n\n{$INCLUDE include.inc}");
    Files.writeString(baseDir.resolve("include.inc"), "\n{$I include.inc}");
    assertParsingErrorMatchingMessage(
        4,
        "Parse error \\(included on line 4 :: line 2:0 Include file '.*include\\.inc' references"
            + " itself\\)");
  }

  @Test
  void testIncludeFileWithCompilerDirectiveExpressionLexerErrorRaisesParsingErrorIssue()
      throws IOException {
    setupFile("\n{$INCLUDE include.inc}");
    Files.writeString(baseDir.resolve("include.inc"), "\n\n{$if 1..2}");
    assertParsingErrorWithMessage(
        2, "Parse error (included on line 2 :: line 3:0 Unexpected '.' in numeric literal)");
  }

  @Test
  void testIncludeFileWithCompilerDirectiveExpressionParserErrorRaisesParsingErrorIssue()
      throws IOException {
    setupFile("\n\n{$INCLUDE include.inc}");
    Files.writeString(baseDir.resolve("include.inc"), "\n\n\n\n{$elseif}");
    assertParsingErrorWithMessage(
        3,
        "Parse error (included on line 3 :: line 5:0 Expected expression. Got '<end of input>')");
  }
}
