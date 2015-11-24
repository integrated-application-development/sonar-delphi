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

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import static org.junit.Assert.*;

public class GrammarTest {

  private static final String BASE_DIR = "/org/sonar/plugins/delphi/grammar/";

  private void parseFile(String fileName) throws IOException {
    System.out.println("Parsing file: " + BASE_DIR + fileName);
    DelphiAST ast = new DelphiAST(DelphiUtils.getResource(BASE_DIR + fileName));
    assertEquals(false, ast.isError());

    String name = fileName.replace(".pas", "");

    String outputFileName = File.createTempFile(name, "").getParentFile().getAbsolutePath() + File.separatorChar + "AST_" + name + ".xml";
    ast.generateXML(outputFileName);
    System.out.println("Generated AST XML file at " + outputFileName);
  }

  @Test
  public void test() throws Exception {
    parseFile("GrammarTest.pas");
  }

  @Test
  public void emptyBeginStatement() throws Exception {
    parseFile("EmptyProcs.pas");
  }

  @Test
  public void parseMultipleAttributes() throws Exception {
    parseFile("MultipleAttributes.pas");
  }

  @Test
  public void parseNewGrammar() throws Exception {
    parseFile("GrammarTestNew.pas");
  }

  @Test
  public void parseComplexArray() throws Exception {
    parseFile("ComplexArray.pas");
  }

  @Test
  public void parseRecordInitialization() throws Exception {
    parseFile("RecordInitialization.pas");
  }


}
