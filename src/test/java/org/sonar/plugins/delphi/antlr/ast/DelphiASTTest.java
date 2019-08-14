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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.antlr.runtime.tree.Tree;
import org.junit.Test;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiASTTest {

  private static final String TEST_FILE = "/org/sonar/plugins/delphi/grammar/GrammarTest.pas";
  private ASTTree ast = new DelphiAST(DelphiUtils.getResource(TEST_FILE));

  @Test
  public void testGenerateXML() throws Exception {
    File xml = File.createTempFile("DelphiAST", ".xml");
    ast.generateXML(xml.getAbsolutePath());

    DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    parser.parse(xml);

    xml.deleteOnExit();
  }

  @Test
  public void testNodesAreExpectedType() {
    checkTypes(ast);
  }

  private void checkTypes(Tree node) {
    for (int i = 0; i < node.getChildCount(); ++i) {
      Tree child = node.getChild(i);
      assertThat(child, instanceOf(DelphiNode.class));
      checkTypes(child);
    }
  }
}
