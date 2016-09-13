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
package org.sonar.plugins.delphi.antlr.ast;

import org.antlr.runtime.RecognitionException;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.fail;

public class DelphiASTTest {

  private static final String TEST_FILE = "/org/sonar/plugins/delphi/grammar/GrammarTest.pas";
  private ASTTree ast;

  @Before
  public void setup() throws IOException, RecognitionException {
    ast = new DelphiAST(DelphiUtils.getResource(TEST_FILE));
  }

  @Test
  public void generateXMLTest() throws IOException {
    File xml = File.createTempFile("DelphiAST", ".xml");
    xml.deleteOnExit();

    ast.generateXML(xml.getAbsolutePath());

    try {
      DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      parser.parse(xml);
    } catch (ParserConfigurationException e) {
      fail("Could not parse generated XML document");
    } catch (SAXException e) {
      fail("Could not parse generated XML document");
    }

  }

}
