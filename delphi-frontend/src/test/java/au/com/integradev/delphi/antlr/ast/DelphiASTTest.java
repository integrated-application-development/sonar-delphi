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
package au.com.integradev.delphi.antlr.ast;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import au.com.integradev.delphi.antlr.ast.node.DelphiNode;
import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import au.com.integradev.delphi.file.DelphiFile;
import au.com.integradev.delphi.utils.DelphiUtils;
import au.com.integradev.delphi.utils.files.DelphiFileUtils;
import net.sourceforge.pmd.lang.ast.Node;
import org.junit.jupiter.api.Test;

class DelphiASTTest {

  private static final String TEST_FILE = "/au/com/integradev/delphi/grammar/GrammarTest.pas";
  private final DelphiAST ast =
      DelphiFile.from(DelphiUtils.getResource(TEST_FILE), DelphiFileUtils.mockConfig()).getAst();

  @Test
  void testAcceptImplemented() {
    DelphiParserVisitor<?> visitor = spy(new DelphiParserVisitor<>() {});
    ast.accept(visitor, null);
    verify(visitor).visit(ast, null);
  }

  @Test
  void testNodesAreExpectedType() {
    checkTypes(ast);
  }

  private static void checkTypes(Node node) {
    assertThat(node).isInstanceOf(DelphiNode.class);
    for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
      Node child = node.jjtGetChild(i);
      checkTypes(child);
    }
  }
}
