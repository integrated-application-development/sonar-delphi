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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import net.sourceforge.pmd.lang.ast.Node;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.utils.builders.DelphiTestFileBuilder;

public class DelphiASTTest {

  private static final String TEST_FILE = "/org/sonar/plugins/delphi/grammar/GrammarTest.pas";
  private DelphiAST ast = DelphiTestFileBuilder.fromResource(TEST_FILE).parse();

  @Test
  public void testAcceptImplemented() {
    DelphiParserVisitor<?> visitor = spy(new DelphiParserVisitor() {});
    ast.accept(visitor, null);
    verify(visitor).visit(ast, null);
  }

  @Test
  public void testNodesAreExpectedType() {
    checkTypes(ast);
  }

  private static void checkTypes(Node node) {
    for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
      Node child = node.jjtGetChild(i);
      assertThat(child, instanceOf(DelphiNode.class));
      checkTypes(child);
    }
  }
}
