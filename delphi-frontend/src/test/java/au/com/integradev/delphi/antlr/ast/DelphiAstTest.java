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

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import au.com.integradev.delphi.file.DelphiFile;
import au.com.integradev.delphi.utils.DelphiUtils;
import au.com.integradev.delphi.utils.files.DelphiFileUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;

class DelphiAstTest {
  private static final String TEST_FILE = "/au/com/integradev/delphi/grammar/GrammarTest.pas";
  private final DelphiAst ast =
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

  @Test
  void testNodeIdsAreUniqueAndStableAcrossEquivalentBuilds() {
    DelphiAst reparsed =
        DelphiFile.from(DelphiUtils.getResource(TEST_FILE), DelphiFileUtils.mockConfig()).getAst();

    List<DelphiNode> originalNodes = flatten(ast);
    List<DelphiNode> reparsedNodes = flatten(reparsed);
    List<Integer> tokenIndices =
        originalNodes.stream().map(DelphiNode::getTokenIndex).collect(Collectors.toList());

    assertThat(originalNodes).hasSameSizeAs(reparsedNodes);
    assertThat(tokenIndices.stream().distinct().count()).isLessThan(tokenIndices.size());
    assertThat(originalNodes).extracting(DelphiNode::getNodeId).doesNotHaveDuplicates();
    assertThat(originalNodes.stream().map(DelphiNode::getNodeId).collect(Collectors.toList()))
        .containsExactlyElementsOf(
            reparsedNodes.stream().map(DelphiNode::getNodeId).collect(Collectors.toList()));
  }

  private static void checkTypes(DelphiNode node) {
    assertThat(node).isInstanceOf(DelphiNode.class);
    for (DelphiNode child : node.getChildren()) {
      checkTypes(child);
    }
  }

  private static List<DelphiNode> flatten(DelphiNode node) {
    List<DelphiNode> nodes = new ArrayList<>();
    flatten(node, nodes);
    return nodes;
  }

  private static void flatten(DelphiNode node, List<DelphiNode> nodes) {
    nodes.add(node);

    for (DelphiNode child : node.getChildren()) {
      flatten(child, nodes);
    }
  }
}
