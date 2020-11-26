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
package org.sonar.plugins.delphi.antlr;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.file.DelphiFile;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiFileConstructionException;
import org.sonar.plugins.delphi.file.DelphiFileConfig;
import org.sonar.plugins.delphi.utils.builders.DelphiTestFileBuilder;

class GrammarTest {
  private static final Logger LOG = Loggers.get(GrammarTest.class);
  private static final String BASE_DIR = "/org/sonar/plugins/delphi/grammar/";
  private DelphiFileConfig fileConfig;

  @BeforeEach
  void setup() {
    fileConfig = DelphiFile.createConfig(UTF_8.name());
  }

  private void parseFile(String fileName) {
    try {
      String path = BASE_DIR + fileName;
      LOG.info("Parsing file: " + path);
      DelphiFile delphiFile = DelphiTestFileBuilder.fromResource(path).delphiFile(fileConfig);

      Source source = new DOMSource(delphiFile.getAst().getAsDocument());
      String prefix = "AST_" + StringUtils.removeEnd(fileName, ".pas");
      File outputFile = File.createTempFile(prefix, ".xml");

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(source, new StreamResult(outputFile));

      LOG.info("Generated AST XML file at: " + outputFile.getName());
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
  void testUndefinedInaccessibleNestedIfDef() {
    fileConfig = DelphiFile.createConfig(UTF_8.name(), Collections.emptyList(), Set.of("Defined"));
    parseFile("UndefinedInaccessibleNestedIfDef.pas");
  }

  @Test
  void testSuperfluousSemicolons() {
    parseFile("SuperfluousSemicolons.pas");
  }

  @Test
  void testEmptyFileShouldThrow() {
    assertThatThrownBy(() -> DelphiTestFileBuilder.fromResource(BASE_DIR + "Emptyfile.pas").parse())
        .isInstanceOf(DelphiFileConstructionException.class);
  }
}
