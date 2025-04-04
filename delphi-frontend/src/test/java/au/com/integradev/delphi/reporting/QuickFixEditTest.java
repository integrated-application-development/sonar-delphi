/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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
package au.com.integradev.delphi.reporting;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.DelphiProperties;
import au.com.integradev.delphi.antlr.DelphiFileStream;
import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.core.Delphi;
import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import au.com.integradev.delphi.file.DelphiFileConfig;
import au.com.integradev.delphi.preprocessor.DelphiPreprocessorFactory;
import au.com.integradev.delphi.preprocessor.search.SearchPath;
import au.com.integradev.delphi.reporting.edits.QuickFixEditImpl;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.utils.DelphiUtils;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFixEdit;

class QuickFixEditTest {
  private static final String TEST_ROOT = "/au/com/integradev/delphi/reporting";
  private static final String TEST_UNIT_PATH = TEST_ROOT + "/TestQuickFixEditFile.pas";

  private static DelphiFileStream getTestStream() {
    try {
      return new DelphiFileStream(
          DelphiUtils.getResource(TEST_UNIT_PATH).getAbsolutePath(), StandardCharsets.UTF_8.name());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void assertEdit(
      TextRangeReplacement actual, FilePosition position, String replacement) {
    assertFilePosition(position, actual.getLocation());
    assertThat(replacement).isEqualTo(actual.getReplacement());
  }

  private static void assertFilePosition(FilePosition actual, FilePosition expected) {
    boolean equals =
        expected.getBeginLine() == actual.getBeginLine()
            && expected.getBeginColumn() == actual.getBeginColumn()
            && expected.getEndLine() == actual.getEndLine()
            && expected.getEndColumn() == actual.getEndColumn();

    assertThat(equals)
        .withFailMessage(
            () ->
                String.format(
                    "Expected [%d:%d to %d:%d], found [%d:%d to %d:%d]",
                    expected.getBeginLine(),
                    expected.getBeginColumn(),
                    expected.getEndLine(),
                    expected.getEndColumn(),
                    actual.getBeginLine(),
                    actual.getBeginColumn(),
                    actual.getEndLine(),
                    actual.getEndColumn()))
        .isTrue();
  }

  private static DelphiNode getNonNullNodeWithImage(DelphiNode parent, String text) {
    for (DelphiNode child : parent.getChildren()) {
      if (child.getImage().equalsIgnoreCase(text)) {
        return child;
      } else {
        DelphiNode grandchildFoo = getNodeWithImage(child, text);
        if (grandchildFoo != null) {
          return grandchildFoo;
        }
      }
    }

    throw new AssertionError("Node with image '" + text + "' does not exist");
  }

  private static DelphiNode getNodeWithImage(DelphiNode parent, String text) {
    for (DelphiNode child : parent.getChildren()) {
      if (child.getImage().equalsIgnoreCase(text)) {
        return child;
      } else {
        DelphiNode grandchildFoo = getNodeWithImage(child, text);
        if (grandchildFoo != null) {
          return grandchildFoo;
        }
      }
    }

    return null;
  }

  private static DelphiAst getTestAst() {
    File baseDir = DelphiUtils.getResource(TEST_ROOT);
    File file = DelphiUtils.getResource(TEST_UNIT_PATH);

    InputFile inputFile;
    try {
      inputFile =
          TestInputFileBuilder.create("moduleKey", baseDir, file)
              .setContents(FileUtils.readFileToString(file, StandardCharsets.UTF_8.name()))
              .setLanguage(Delphi.KEY)
              .setType(InputFile.Type.MAIN)
              .build();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    var preprocessorFactory =
        new DelphiPreprocessorFactory(DelphiProperties.COMPILER_VERSION_DEFAULT, Platform.WINDOWS);
    var typeFactory =
        new TypeFactoryImpl(
            DelphiProperties.COMPILER_TOOLCHAIN_DEFAULT, DelphiProperties.COMPILER_VERSION_DEFAULT);

    DelphiFileConfig fileConfig = mock(DelphiFileConfig.class);
    when(fileConfig.getEncoding()).thenReturn(StandardCharsets.UTF_8.name());
    when(fileConfig.getPreprocessorFactory()).thenReturn(preprocessorFactory);
    when(fileConfig.getTypeFactory()).thenReturn(typeFactory);
    when(fileConfig.getSearchPath()).thenReturn(SearchPath.create(Collections.emptyList()));
    when(fileConfig.getDefinitions()).thenReturn(Collections.emptySet());

    var mainFile = DelphiInputFile.from(inputFile, fileConfig);
    return mainFile.getAst();
  }

  private static FilePosition startOf(DelphiNode node) {
    return FilePosition.from(
        node.getBeginLine(), node.getBeginColumn(), node.getBeginLine(), node.getBeginColumn());
  }

  private static FilePosition endOf(DelphiNode node) {
    return FilePosition.from(
        node.getEndLine(), node.getEndColumn(), node.getEndLine(), node.getEndColumn());
  }

  List<TextRangeReplacement> getTextEdits(QuickFixEdit edit) {
    return ((QuickFixEditImpl) edit).toTextEdits(() -> fileStream);
  }

  private DelphiFileStream fileStream;
  private DelphiAst ast;

  @BeforeEach
  void beforeEach() {
    fileStream = getTestStream();
    ast = getTestAst();
  }

  @Test
  void testInsert() {
    QuickFixEdit insert = QuickFixEdit.insert("inherited ", 9, 2);

    List<TextRangeReplacement> edits = getTextEdits(insert);
    assertThat(edits).hasSize(1);
    assertEdit(edits.get(0), FilePosition.from(9, 2, 9, 2), "inherited ");
  }

  @Test
  void testInsertBefore() {
    DelphiNode node = getNonNullNodeWithImage(ast, "Bar");
    QuickFixEdit insert = QuickFixEdit.insertBefore("inherited ", node);

    List<TextRangeReplacement> edits = getTextEdits(insert);
    assertThat(edits).hasSize(1);
    assertEdit(edits.get(0), FilePosition.from(9, 2, 9, 2), "inherited ");
  }

  @Test
  void testInsertAfter() {
    DelphiNode node = getNonNullNodeWithImage(ast, "Bar");
    QuickFixEdit insert = QuickFixEdit.insertAfter(" baz", node);

    List<TextRangeReplacement> edits = getTextEdits(insert);
    assertThat(edits).hasSize(1);
    assertEdit(edits.get(0), FilePosition.from(9, 5, 9, 5), " baz");
  }

  @Test
  void testCopy() {
    DelphiNode node = getNonNullNodeWithImage(ast, "Bar");
    QuickFixEdit copy = QuickFixEdit.copy(node, FilePosition.from(7, 10, 7, 10));

    List<TextRangeReplacement> edits = getTextEdits(copy);
    assertThat(edits).hasSize(1);
    assertFilePosition(edits.get(0).getLocation(), FilePosition.from(7, 10, 7, 10));
    assertEdit(edits.get(0), FilePosition.from(7, 10, 7, 10), "Bar");
  }

  @Test
  void testCopyBefore() {
    DelphiNode node = getNonNullNodeWithImage(ast, "Bar");
    DelphiNode referenceNode = getNonNullNodeWithImage(ast, "Foo");

    QuickFixEdit copy = QuickFixEdit.copyBefore(node, referenceNode);

    List<TextRangeReplacement> edits = getTextEdits(copy);
    assertThat(edits).hasSize(1);
    assertEdit(edits.get(0), startOf(referenceNode), "Bar");
  }

  @Test
  void testCopyAfter() {
    DelphiNode node = getNonNullNodeWithImage(ast, "Bar");
    DelphiNode referenceNode = getNonNullNodeWithImage(ast, "Foo");

    QuickFixEdit copy = QuickFixEdit.copyAfter(node, referenceNode);

    List<TextRangeReplacement> edits = getTextEdits(copy);
    assertThat(edits).hasSize(1);
    assertEdit(edits.get(0), endOf(referenceNode), "Bar");
  }

  @Test
  void testCopyReplacing() {
    DelphiNode node = getNonNullNodeWithImage(ast, "Bar");
    DelphiNode referenceNode = getNonNullNodeWithImage(ast, "Foo");

    QuickFixEdit copy = QuickFixEdit.copyReplacing(node, referenceNode);

    List<TextRangeReplacement> edits = getTextEdits(copy);
    assertThat(edits).hasSize(1);
    assertEdit(edits.get(0), FilePosition.from(referenceNode), "Bar");
  }

  @Test
  void testMove() {
    DelphiNode node = getNonNullNodeWithImage(ast, "Bar");
    QuickFixEdit move = QuickFixEdit.move(node, FilePosition.from(7, 10, 7, 10));

    List<TextRangeReplacement> edits = getTextEdits(move);
    assertThat(edits).hasSize(2);
    assertEdit(edits.get(0), FilePosition.from(7, 10, 7, 10), "Bar");
    assertEdit(edits.get(1), FilePosition.from(node), "");
  }

  @Test
  void testMoveBefore() {
    DelphiNode node = getNonNullNodeWithImage(ast, "Bar");
    DelphiNode referenceNode = getNonNullNodeWithImage(ast, "Foo");

    QuickFixEdit move = QuickFixEdit.moveBefore(node, referenceNode);

    List<TextRangeReplacement> edits = getTextEdits(move);
    assertThat(edits).hasSize(2);
    assertEdit(edits.get(0), startOf(referenceNode), "Bar");
    assertEdit(edits.get(1), FilePosition.from(node), "");
  }

  @Test
  void testMoveAfter() {
    DelphiNode node = getNonNullNodeWithImage(ast, "Bar");
    DelphiNode referenceNode = getNonNullNodeWithImage(ast, "Foo");

    QuickFixEdit move = QuickFixEdit.moveAfter(node, referenceNode);

    List<TextRangeReplacement> edits = getTextEdits(move);
    assertThat(edits).hasSize(2);
    assertEdit(edits.get(0), endOf(referenceNode), "Bar");
    assertEdit(edits.get(1), FilePosition.from(node), "");
  }

  @Test
  void testMoveReplacing() {
    DelphiNode node = getNonNullNodeWithImage(ast, "Bar");
    DelphiNode referenceNode = getNonNullNodeWithImage(ast, "Foo");

    QuickFixEdit move = QuickFixEdit.moveReplacing(node, referenceNode);

    List<TextRangeReplacement> edits = getTextEdits(move);
    assertThat(edits).hasSize(2);
    assertEdit(edits.get(0), FilePosition.from(referenceNode), "Bar");
    assertEdit(edits.get(1), FilePosition.from(node), "");
  }

  @Test
  void testDeleteNode() {
    DelphiNode node = getNonNullNodeWithImage(ast, "Bar");
    QuickFixEdit delete = QuickFixEdit.delete(node);

    List<TextRangeReplacement> edits = getTextEdits(delete);
    assertThat(edits).hasSize(1);
    assertEdit(edits.get(0), FilePosition.from(node), "");
  }

  @Test
  void testDeleteRange() {
    FilePosition position = FilePosition.from(7, 10, 7, 13);
    QuickFixEdit delete = QuickFixEdit.delete(position);

    List<TextRangeReplacement> edits = getTextEdits(delete);
    assertThat(edits).hasSize(1);
    assertEdit(edits.get(0), position, "");
  }
}
