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
package au.com.integradev.delphi.antlr;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.file.DelphiFile;
import au.com.integradev.delphi.file.DelphiFile.DelphiFileConstructionException;
import au.com.integradev.delphi.file.DelphiFileConfig;
import au.com.integradev.delphi.preprocessor.DelphiPreprocessorFactory;
import au.com.integradev.delphi.preprocessor.search.SearchPath;
import au.com.integradev.delphi.utils.DelphiUtils;
import au.com.integradev.delphi.utils.files.DelphiFileUtils;
import au.com.integradev.delphi.utils.types.TypeFactoryUtils;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

class GrammarTest {
  private static final Logger LOG = Loggers.get(GrammarTest.class);
  private static final String BASE_DIR = "/au/com/integradev/delphi/grammar/";
  private DelphiFileConfig fileConfig;

  @BeforeEach
  void setup() {
    fileConfig = DelphiFileUtils.mockConfig();
  }

  private void parseFile(String fileName) {
    try {
      String path = BASE_DIR + fileName;
      LOG.info("Parsing file: " + path);
      DelphiFile.from(DelphiUtils.getResource(path), fileConfig);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  @Test
  void testEmptyBeginStatement() {
    parseFile("EmptyProcs.pas");
  }

  @Test
  void testParseMultipleAttributes() {
    parseFile("MultipleAttributes.pas");
  }

  @Test
  void testParseRecordInitialization() {
    parseFile("RecordInitialization.pas");
  }

  @Test
  void testParseRecordConstructor() {
    parseFile("RecordConstructor.pas");
  }

  @Test
  void testParseLabel() {
    parseFile("LabelUsage.pas");
  }

  @Test
  void testParseDUnitX() {
    parseFile("DUnitX.pas");
  }

  @Test
  void testParseUTF8FileWithBOM() {
    parseFile("UTF8WithBOM.pas");
  }

  @Test
  void testParseAnonymousMethods() {
    parseFile("AnonymousMethods.pas");
  }

  @Test
  void testParseGenerics() {
    parseFile("Generics.pas");
  }

  @Test
  void testParseKeyWordsAsIdentifier() {
    parseFile("KeyWordsAsIdentifier.pas");
  }

  @Test
  void testParseListUtils() {
    parseFile("ListUtils.pas");
  }

  @Test
  void testParseEmptyNestedType() {
    parseFile("EmptyNestedType.pas");
  }

  @Test
  void testParseEmptyClassDeclarations() {
    parseFile("EmptyClassDeclarations.pas");
  }

  @Test
  void testSubRangeTypes() {
    parseFile("SubRangeTypes.pas");
  }

  @Test
  void testMethodProcDirectives() {
    parseFile("MethodProcDirectives.pas");
  }

  @Test
  void testOptionalFunctionReturnType() {
    parseFile("OptionalFunctionReturnType.pas");
  }

  @Test
  void testConstExpressionAmbiguity() {
    parseFile("ConstExpressionAmbiguity.pas");
  }

  @Test
  void testVariantRecord() {
    parseFile("VariantRecord.pas");
  }

  @Test
  void testEmptyCaseItem() {
    parseFile("EmptyCaseItem.pas");
  }

  @Test
  void testRecordHelperConstants() {
    parseFile("RecordHelperConstants.pas");
  }

  @Test
  void testGenericSubTypeDecl() {
    parseFile("GenericSubTypeDecl.pas");
  }

  @Test
  void testArrayIndices() {
    parseFile("ArrayIndices.pas");
  }

  @Test
  void testNumberNotConsumedByIdentifiers() {
    parseFile("NumberNotConsumedByIdentifiers.pas");
  }

  @Test
  void testRaiseCastedException() {
    parseFile("RaiseCastedException.pas");
  }

  @Test
  void testQualifiedKeywordIdentifier() {
    parseFile("QualifiedKeywordIdentifier.pas");
  }

  @Test
  void testInlineVars() {
    parseFile("InlineVars.pas");
  }

  @Test
  void testUnusualBrackets() {
    parseFile("UnusualBrackets.pas");
  }

  @Test
  void testDoubleAmpersands() {
    parseFile("DoubleAmpersands.pas");
  }

  @Test
  void testUndefinedInaccessibleNestedIfDef() {
    fileConfig =
        DelphiFile.createConfig(
            StandardCharsets.UTF_8.name(),
            new DelphiPreprocessorFactory(Platform.WINDOWS),
            TypeFactoryUtils.defaultFactory(),
            SearchPath.create(Collections.emptyList()),
            Set.of("Defined"));
    parseFile("UndefinedInaccessibleNestedIfDef.pas");
  }

  @Test
  void testSuperfluousSemicolons() {
    parseFile("SuperfluousSemicolons.pas");
  }

  @Test
  void testClassOperators() {
    parseFile("ClassOperators.pas");
  }

  @Test
  void testEmptyFileShouldThrow() {
    assertThatThrownBy(
            () ->
                DelphiFile.from(
                    DelphiUtils.getResource(BASE_DIR + "EmptyFile.pas"),
                    DelphiFileUtils.mockConfig()))
        .isInstanceOf(DelphiFileConstructionException.class);
  }
}
