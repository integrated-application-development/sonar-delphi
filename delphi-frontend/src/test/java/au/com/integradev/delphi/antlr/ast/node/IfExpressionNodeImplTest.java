/*
 * Sonar Delphi Plugin
 * Copyright (C) 2026 Integrated Application Development
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
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;

class IfExpressionNodeImplTest {
  @Test
  void testBranches() {
    IfExpressionNode node = parse("Foo := if Bar then Baz else Flarp;");

    assertThat(node.getGuardExpression().getImage()).isEqualTo("Bar");
    assertThat(node.getThenExpression().getImage()).isEqualTo("Baz");
    assertThat(node.getElseExpression().getImage()).isEqualTo("Flarp");
  }

  @Test
  void testImage() {
    IfExpressionNode node = parse("Foo := if Bar then Baz else Flarp;");

    assertThat(node.getImage()).isEqualTo("if Bar then Baz else Flarp");
  }

  @Test
  void testTokenType() {
    IfExpressionNode node = parse("Foo := if Bar then Baz else Flarp;");

    assertThat(node.getTokenType()).isEqualTo(DelphiTokenType.IF);
  }

  @Test
  void testElseBranchBindsToNearestIf() {
    IfExpressionNode node = parse("Foo := if Bar then Baz else if Flarp then Bim else Bum;");

    assertThat(node.getThenExpression().getImage()).isEqualTo("Baz");
    assertThat(node.getElseExpression()).isInstanceOf(IfExpressionNode.class);
    assertThat(node.getElseExpression().getImage()).isEqualTo("if Flarp then Bim else Bum");
  }

  @Test
  void testUnparenthesizedNestedIfInThenBranch() {
    IfExpressionNode node = parse("Foo := if Bar then if Baz then Bim else Bum else Flarp;");

    assertThat(node.getThenExpression()).isInstanceOf(IfExpressionNode.class);
    assertThat(node.getThenExpression().getImage()).isEqualTo("if Baz then Bim else Bum");
    assertThat(node.getElseExpression().getImage()).isEqualTo("Flarp");
  }

  private static IfExpressionNode parse(String statement) {
    return DelphiFileUtils.parse(
            "unit Test;",
            "",
            "interface",
            "",
            "implementation",
            "",
            "procedure Test;",
            "begin",
            "  " + statement,
            "end;",
            "",
            "end.")
        .getAst()
        .getFirstDescendantOfType(IfExpressionNode.class);
  }
}
