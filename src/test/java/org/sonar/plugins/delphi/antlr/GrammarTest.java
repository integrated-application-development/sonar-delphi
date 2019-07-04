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


import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.filestream.DelphiFileStreamConfig;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class GrammarTest {
  private static final Logger LOG = Loggers.get(GrammarTest.class);
  private static final String BASE_DIR = "/org/sonar/plugins/delphi/grammar/";
  private final DelphiFileStreamConfig fileStreamConfig = new DelphiFileStreamConfig("UTF-8");

  private void parseFile(String fileName) throws IOException {
    String path = BASE_DIR + fileName;
    LOG.info("Parsing file: " + path);
    File file = DelphiUtils.getResource(path);
    DelphiAST ast = new DelphiAST(file, fileStreamConfig);
    assertFalse(ast.isError());

    String name = fileName.replace(".pas", "");

    String outputFileName =
        File.createTempFile(name, "").getParentFile().getAbsolutePath() + File.separatorChar
            + "AST_" + name + ".xml";
    ast.generateXML(outputFileName);
    LOG.info("Generated AST XML file at " + outputFileName);
  }

  @Test
  public void testEmptyBeginStatement() throws IOException {
    parseFile("EmptyProcs.pas");
  }

  @Test
  public void testParseMultipleAttributes() throws IOException {
    parseFile("MultipleAttributes.pas");
  }

  @Test
  public void testParseRecordInitialization() throws IOException {
    parseFile("RecordInitialization.pas");
  }

  @Test
  public void testParseRecordConstructor() throws IOException {
    parseFile("RecordConstructor.pas");
  }

  @Test
  public void testParseLabel() throws IOException {
    parseFile("LabelUsage.pas");
  }

  @Test
  public void testParseDUnitX() throws IOException {
    parseFile("DUnitX.pas");
  }

  @Test
  public void testParseUTF8FileWithBOM() throws IOException {
    parseFile("UTF8WithBOM.pas");
  }

  @Test
  public void testParseAnonymousMethods() throws IOException {
    parseFile("AnonymousMethods.pas");
  }

  @Test
  public void testParseGenerics() throws IOException {
    parseFile("Generics.pas");
  }

  @Test
  public void testParseKeyWordsAsIdentifier() throws IOException {
    parseFile("KeyWordsAsIdentifier.pas");
  }

  @Test
  public void testParseListUtils() throws IOException {
    parseFile("ListUtils.pas");
  }

  @Test
  public void testParseEmptyNestedType() throws IOException {
    parseFile("EmptyNestedType.pas");
  }

  @Test
  public void testParseEmptyClassDeclarations() throws IOException {
    parseFile("EmptyClassDeclarations.pas");
  }

  @Test
  public void testSubRangeTypes() throws IOException {
    parseFile("SubRangeTypes.pas");
  }

  @Test
  public void testMethodProcDirectives() throws IOException {
    parseFile("MethodProcDirectives.pas");
  }

  @Test
  public void testOptionalFunctionReturnType() throws IOException {
    parseFile("OptionalFunctionReturnType.pas");
  }

  @Test
  public void testConstExpressionAmbiguity() throws IOException {
    parseFile("ConstExpressionAmbiguity.pas");
  }

  @Test
  public void testVariantRecord() throws IOException {
    parseFile("VariantRecord.pas");
  }

  @Test
  public void testEmptyCaseItem() throws IOException {
    parseFile("EmptyCaseItem.pas");
  }

  @Test
  public void testRecordHelperConstants() throws IOException {
    parseFile("RecordHelperConstants.pas");
  }

  @Test
  public void testGenericSubTypeDecl() throws IOException {
    parseFile("GenericSubTypeDecl.pas");
  }

  @Test
  public void testArrayIndices() throws IOException {
    parseFile("ArrayIndices.pas");
  }

  @Test
  public void testNumberNotConsumedByIdentifiers() throws IOException {
    parseFile("NumberNotConsumedByIdentifiers.pas");
  }

  @Test
  public void testRaiseCastedException() throws IOException {
    parseFile("RaiseCastedException.pas");
  }

  @Test
  public void testQualifiedKeywordIdentifier() throws IOException {
    parseFile("QualifiedKeywordIdentifier.pas");
  }

  /*
   * A bizarre corner-case in which an undefined ifdef nested inside of a defined ifdef would cause parsing errors...
   * But only if the nested ifdef was commented out or inside a string.
   */
  @Test
  public void testUndefinedInaccessibleNestedIfDef() throws IOException {
    fileStreamConfig.getDefinitions().add("Defined");
    parseFile("UndefinedInaccessibleNestedIfDef.pas");
    fileStreamConfig.getDefinitions().clear();
  }
}
