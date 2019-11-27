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

import java.io.File;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.antlr.filestream.DelphiFileStreamConfig;
import org.sonar.plugins.delphi.file.DelphiFile;
import org.sonar.plugins.delphi.utils.builders.DelphiTestFileBuilder;

public class GrammarTest {
  private static final Logger LOG = Loggers.get(GrammarTest.class);
  private static final String BASE_DIR = "/org/sonar/plugins/delphi/grammar/";
  private DelphiFileStreamConfig fileStreamConfig;

  @Before
  public void setup() {
    fileStreamConfig = new DelphiFileStreamConfig(UTF_8.name());
  }

  private void parseFile(String fileName) {
    try {
      String path = BASE_DIR + fileName;
      LOG.info("Parsing file: " + path);
      DelphiFile delphiFile = DelphiTestFileBuilder.fromResource(path).delphiFile(fileStreamConfig);

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
  public void testEmptyBeginStatement() {
    parseFile("EmptyProcs.pas");
  }

  @Test
  public void testParseMultipleAttributes() {
    parseFile("MultipleAttributes.pas");
  }

  @Test
  public void testParseRecordInitialization() {
    parseFile("RecordInitialization.pas");
  }

  @Test
  public void testParseRecordConstructor() {
    parseFile("RecordConstructor.pas");
  }

  @Test
  public void testParseLabel() {
    parseFile("LabelUsage.pas");
  }

  @Test
  public void testParseDUnitX() {
    parseFile("DUnitX.pas");
  }

  @Test
  public void testParseUTF8FileWithBOM() {
    parseFile("UTF8WithBOM.pas");
  }

  @Test
  public void testParseAnonymousMethods() {
    parseFile("AnonymousMethods.pas");
  }

  @Test
  public void testParseGenerics() {
    parseFile("Generics.pas");
  }

  @Test
  public void testParseKeyWordsAsIdentifier() {
    parseFile("KeyWordsAsIdentifier.pas");
  }

  @Test
  public void testParseListUtils() {
    parseFile("ListUtils.pas");
  }

  @Test
  public void testParseEmptyNestedType() {
    parseFile("EmptyNestedType.pas");
  }

  @Test
  public void testParseEmptyClassDeclarations() {
    parseFile("EmptyClassDeclarations.pas");
  }

  @Test
  public void testSubRangeTypes() {
    parseFile("SubRangeTypes.pas");
  }

  @Test
  public void testMethodProcDirectives() {
    parseFile("MethodProcDirectives.pas");
  }

  @Test
  public void testOptionalFunctionReturnType() {
    parseFile("OptionalFunctionReturnType.pas");
  }

  @Test
  public void testConstExpressionAmbiguity() {
    parseFile("ConstExpressionAmbiguity.pas");
  }

  @Test
  public void testVariantRecord() {
    parseFile("VariantRecord.pas");
  }

  @Test
  public void testEmptyCaseItem() {
    parseFile("EmptyCaseItem.pas");
  }

  @Test
  public void testRecordHelperConstants() {
    parseFile("RecordHelperConstants.pas");
  }

  @Test
  public void testGenericSubTypeDecl() {
    parseFile("GenericSubTypeDecl.pas");
  }

  @Test
  public void testArrayIndices() {
    parseFile("ArrayIndices.pas");
  }

  @Test
  public void testNumberNotConsumedByIdentifiers() {
    parseFile("NumberNotConsumedByIdentifiers.pas");
  }

  @Test
  public void testRaiseCastedException() {
    parseFile("RaiseCastedException.pas");
  }

  @Test
  public void testQualifiedKeywordIdentifier() {
    parseFile("QualifiedKeywordIdentifier.pas");
  }

  /*
   * A bizarre corner-case in which an undefined ifdef nested inside of a defined ifdef would cause parsing errors...
   * But only if the nested ifdef was commented out or inside a string.
   */
  @Test
  public void testUndefinedInaccessibleNestedIfDef() {
    fileStreamConfig.getDefinitions().add("Defined");
    parseFile("UndefinedInaccessibleNestedIfDef.pas");
  }

  @Test
  public void testSuperfluousSemicolons() {
    parseFile("SuperfluousSemicolons.pas");
  }
}
