/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.antlr.ast.node;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.integradev.delphi.utils.files.DelphiFileUtils;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.ast.IfExpressionNode;

class IfExpressionNodeImplTest {
  @Test
  void testBranchesShouldBeFound() {
    IfExpressionNode node =
        parse(
            "procedure Test;", //
            "begin",
            "  Result := if True then 1 else 2;",
            "end;");

    assertThat(node.getGuardExpression().getImage()).isEqualTo("True");
    assertThat(node.getThenExpression().getImage()).isEqualTo("1");
    assertThat(node.getElseExpression().getImage()).isEqualTo("2");
  }

  @Test
  void testImageShouldReconstructExpression() {
    IfExpressionNode node =
        parse(
            "procedure Test;", //
            "begin",
            "  Result := if True then 1 else 2;",
            "end;");

    assertThat(node.getImage()).isEqualTo("if True then 1 else 2");
  }

  @Test
  void testNestedElseBranchShouldParseAsChain() {
    IfExpressionNode node =
        parse(
            "procedure Test;", //
            "begin",
            "  Result := if True then 1 else if False then 2 else 3;",
            "end;");

    assertThat(node.getElseExpression()).isInstanceOf(IfExpressionNode.class);
  }

  @Test
  void testIntegerAndRealBranchesShouldResolveToRealType() {
    IfExpressionNode node =
        parse(
            "procedure Test;", //
            "begin",
            "  Result := if True then 1 else 2.0;",
            "end;");

    assertThat(node.getType().isReal()).isTrue();
  }

  @Test
  void testIncompatibleBranchesShouldResolveToUnknownType() {
    IfExpressionNode node =
        parse(
            "procedure Test;", //
            "begin",
            "  Result := if True then 1 else 'Two';",
            "end;");

    assertThat(node.getType().isUnknown()).isTrue();
  }

  private static IfExpressionNode parse(String... lines) {
    return DelphiFileUtils.parse(
            "unit Test;",
            "",
            "interface",
            "",
            "implementation",
            "",
            lines.length == 0 ? "" : String.join("\n", lines),
            "",
            "end.")
        .getAst()
        .getFirstDescendantOfType(IfExpressionNode.class);
  }
}
