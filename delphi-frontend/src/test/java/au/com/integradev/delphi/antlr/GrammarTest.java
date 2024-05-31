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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GrammarTest {
  private static final Logger LOG = LoggerFactory.getLogger(GrammarTest.class);
  private static final String BASE_DIR = "/au/com/integradev/delphi/grammar/";
  private DelphiFileConfig fileConfig;

  @BeforeEach
  void setup() {
    fileConfig = DelphiFileUtils.mockConfig();
  }

  private void parse(String fileName) {
    String path = BASE_DIR + fileName;
    LOG.info("Parsing file: {}", path);
    DelphiFile.from(DelphiUtils.getResource(path), fileConfig);
  }

  private void assertParsed(String fileName) {
    try {
      parse(fileName);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  @Test
  void testMultilineStrings() {
    assertParsed("MultilineStrings.pas");
  }

  @Test
  void testMultilineLookalikeStrings() {
    assertParsed("MultilineLookalikeStrings.pas");
  }

  @Test
  void testMultilineInvalidButAcceptedStrings() {
    assertParsed("MultilineInvalidButAcceptedStrings.pas");
  }

  @Test
  void testEmptyBeginStatement() {
    assertParsed("EmptyProcs.pas");
  }

  @Test
  void testAttributesUnit() {
    assertParsed("AttributesUnit.pas");
  }

  @Test
  void testAttributesProgram() {
    assertParsed("AttributesProgram.dpr");
  }

  @Test
  void testAttributesLibrary() {
    assertParsed("AttributesLibrary.dpr");
  }

  @Test
  void testAttributesPackage() {
    assertParsed("AttributesPackage.dpk");
  }

  @Test
  void testParseMultipleAttributes() {
    assertParsed("MultipleAttributes.pas");
  }

  @Test
  void testParseRecordInitialization() {
    assertParsed("RecordInitialization.pas");
  }

  @Test
  void testParseRecordConstructor() {
    assertParsed("RecordConstructor.pas");
  }

  @Test
  void testParseLabel() {
    assertParsed("LabelUsage.pas");
  }

  @Test
  void testParseDUnitX() {
    assertParsed("DUnitX.pas");
  }

  @Test
  void testParseUTF8FileWithBOM() {
    assertParsed("UTF8WithBOM.pas");
  }

  @Test
  void testParseAnonymousMethods() {
    assertParsed("AnonymousMethods.pas");
  }

  @Test
  void testParseGenerics() {
    assertParsed("Generics.pas");
  }

  @Test
  void testParseKeyWordsAsIdentifier() {
    assertParsed("KeyWordsAsIdentifier.pas");
  }

  @Test
  void testParseExportsSection() {
    assertParsed("ExportsSection.dpr");
  }

  @Test
  void testParseEmptyProgram() {
    assertParsed("EmptyProgram.dpr");
  }

  @Test
  void testParseProgramWithEmptyBlock() {
    assertParsed("ProgramWithEmptyBlock.dpr");
  }

  @Test
  void testParseEnd() {
    assertParsed("End.dpr");
  }

  @Test
  void testParseListUtils() {
    assertParsed("ListUtils.pas");
  }

  @Test
  void testParseEmptyNestedType() {
    assertParsed("EmptyNestedType.pas");
  }

  @Test
  void testParseEmptyClassDeclarations() {
    assertParsed("EmptyClassDeclarations.pas");
  }

  @Test
  void testSubRangeTypes() {
    assertParsed("SubRangeTypes.pas");
  }

  @Test
  void testMethodProcDirectives() {
    assertParsed("MethodProcDirectives.pas");
  }

  @Test
  void testWinapiCallingConvention() {
    assertParsed("WinapiCallingConvention.pas");
  }

  @Test
  void testOptionalFunctionReturnType() {
    assertParsed("OptionalFunctionReturnType.pas");
  }

  @Test
  void testConstExpressionAmbiguity() {
    assertParsed("ConstExpressionAmbiguity.pas");
  }

  @Test
  void testVariantRecord() {
    assertParsed("VariantRecord.pas");
  }

  @Test
  void testEmptyCaseItem() {
    assertParsed("EmptyCaseItem.pas");
  }

  @Test
  void testArrayWithAnonymousMethods() {
    assertParsed("ArrayWithAnonymousMethods.pas");
  }

  @Test
  void testRecordHelperConstants() {
    assertParsed("RecordHelperConstants.pas");
  }

  @Test
  void testGenericDescendantDecl() {
    assertParsed("GenericDescendantDecl.pas");
  }

  @Test
  void testArrayIndices() {
    assertParsed("ArrayIndices.pas");
  }

  @Test
  void testNumberNotConsumedByIdentifiers() {
    assertParsed("NumberNotConsumedByIdentifiers.pas");
  }

  @Test
  void testRaiseCastedException() {
    assertParsed("RaiseCastedException.pas");
  }

  @Test
  void testQualifiedKeywordIdentifier() {
    assertParsed("QualifiedKeywordIdentifier.pas");
  }

  @Test
  void testInlineVars() {
    assertParsed("InlineVars.pas");
  }

  @Test
  void testUnusualBrackets() {
    assertParsed("UnusualBrackets.pas");
  }

  @Test
  void testDoubleAmpersands() {
    assertParsed("DoubleAmpersands.pas");
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
    assertParsed("UndefinedInaccessibleNestedIfDef.pas");
  }

  @Test
  void testSuperfluousSemicolons() {
    assertParsed("SuperfluousSemicolons.pas");
  }

  @Test
  void testClassOperators() {
    assertParsed("ClassOperators.pas");
  }

  @Test
  void testUnusualWhitespace() {
    assertParsed("UnusualWhitespace.pas");
  }

  @Test
  void testUnusualIntegerLiterals() {
    assertParsed("UnusualIntegerLiterals.pas");
  }

  @Test
  void testAmpersandNumericLiterals() {
    assertParsed("AmpersandNumericLiterals.pas");
  }

  @Test
  void testEmptyFileShouldThrow() {
    assertThatThrownBy(() -> parse("EmptyFile.pas"))
        .isInstanceOf(DelphiFileConstructionException.class);
  }

  @Test
  void testUnicodeIdentifiers() {
    assertParsed("UnicodeIdentifiers.pas");
  }

  @Test
  void testFullWidthNumeralAtStartOfIdentifierShouldThrow() {
    assertThatThrownBy(() -> parse("FullWidthNumeralBeforeIdentifier.pas"))
        .isInstanceOf(DelphiFileConstructionException.class)
        .hasCauseInstanceOf(DelphiParser.ParserException.class)
        .hasMessageContaining("line 6:2 mismatched input '０' expecting IMPLEMENTATION");
  }

  @Test
  void testNamedArguments() {
    assertParsed("NamedArguments.pas");
  }
}
