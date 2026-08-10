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
import org.sonar.plugins.communitydelphi.api.ast.ArrayConstructorNode;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.UnaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.operator.BinaryOperator;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;

class BinaryExpressionNodeImplTest {
  @Test
  void testIsNot() {
    BinaryExpressionNode node = parse("Foo := Bar is not TBaz;");

    assertThat(node.getOperator()).isEqualTo(BinaryOperator.IS_NOT);
    assertThat(node.getTokenType()).isEqualTo(DelphiTokenType.BINARY_EXPRESSION);
    assertThat(node.getOperatorNode().getTokenType()).isEqualTo(DelphiTokenType.IS_NOT);
    assertThat(node.getOperatorNode().getOperator()).isEqualTo(BinaryOperator.IS_NOT);
    assertThat(node.getOperatorNode().getImage()).isEqualTo("is not");
    assertThat(node.getOperatorNode().getChild(0).getTokenType()).isEqualTo(DelphiTokenType.IS);
    assertThat(node.getOperatorNode().getChild(1).getTokenType()).isEqualTo(DelphiTokenType.NOT);
    assertThat(node.getLeft().getImage()).isEqualTo("Bar");
    assertThat(node.getRight().getImage()).isEqualTo("TBaz");
    assertThat(node.getImage()).isEqualTo("Bar is not TBaz");
  }

  @Test
  void testIsNotWithCommentBetweenKeywords() {
    BinaryExpressionNode node = parse("Foo := Bar is {comment} not TBaz;");

    assertThat(node.getOperator()).isEqualTo(BinaryOperator.IS_NOT);
    assertThat(node.getOperatorNode().getImage()).isEqualTo("is not");
  }

  @Test
  void testNotIn() {
    BinaryExpressionNode node = parse("Foo := Bar not in Baz;");

    assertThat(node.getOperator()).isEqualTo(BinaryOperator.NOT_IN);
    assertThat(node.getTokenType()).isEqualTo(DelphiTokenType.BINARY_EXPRESSION);
    assertThat(node.getOperatorNode().getTokenType()).isEqualTo(DelphiTokenType.NOT_IN);
    assertThat(node.getOperatorNode().getOperator()).isEqualTo(BinaryOperator.NOT_IN);
    assertThat(node.getOperatorNode().getImage()).isEqualTo("not in");
    assertThat(node.getOperatorNode().getChild(0).getTokenType()).isEqualTo(DelphiTokenType.NOT);
    assertThat(node.getOperatorNode().getChild(1).getTokenType()).isEqualTo(DelphiTokenType.IN);
    assertThat(node.getLeft().getImage()).isEqualTo("Bar");
    assertThat(node.getRight().getImage()).isEqualTo("Baz");
    assertThat(node.getImage()).isEqualTo("Bar not in Baz");
  }

  @Test
  void testUnaryNotBindsTighterThanIn() {
    BinaryExpressionNode node = parse("Foo := not Bar in Baz;");

    assertThat(node.getOperator()).isEqualTo(BinaryOperator.IN);
    assertThat(node.getLeft()).isInstanceOf(UnaryExpressionNode.class);
    assertThat(node.getLeft().getImage()).isEqualTo("not Bar");
  }

  @Test
  void testNotInInsideArrayConstructor() {
    BinaryExpressionNode node = parse("Foo := True in [Bar not in [1], False];");

    assertThat(node.getOperator()).isEqualTo(BinaryOperator.IN);

    ArrayConstructorNode arrayConstructor =
        node.getRight().getFirstDescendantOfType(ArrayConstructorNode.class);
    assertThat(arrayConstructor.getElements()).hasSize(2);

    ExpressionNode firstElement = arrayConstructor.getElements().get(0);
    assertThat(firstElement).isInstanceOf(BinaryExpressionNode.class);
    assertThat(((BinaryExpressionNode) firstElement).getOperator())
        .isEqualTo(BinaryOperator.NOT_IN);
  }

  @Test
  void testNotInChainsLeftAssociatively() {
    BinaryExpressionNode node = parse("Foo := Bar not in Baz not in Flarp;");

    assertThat(node.getOperator()).isEqualTo(BinaryOperator.NOT_IN);
    assertThat(node.getRight().getImage()).isEqualTo("Flarp");
    assertThat(node.getLeft()).isInstanceOf(BinaryExpressionNode.class);
    assertThat(((BinaryExpressionNode) node.getLeft()).getOperator())
        .isEqualTo(BinaryOperator.NOT_IN);
  }

  @Test
  void testUnaryNotBindsTighterThanNotIn() {
    BinaryExpressionNode node = parse("Foo := not Bar not in Baz;");

    assertThat(node.getOperator()).isEqualTo(BinaryOperator.NOT_IN);
    assertThat(node.getLeft()).isInstanceOf(UnaryExpressionNode.class);
    assertThat(node.getLeft().getImage()).isEqualTo("not Bar");
  }

  @Test
  void testAsBindsTighterThanIsNot() {
    BinaryExpressionNode node = parse("Foo := Bar as TBaz is not TFlarp;");

    assertThat(node.getOperator()).isEqualTo(BinaryOperator.IS_NOT);
    assertThat(node.getRight().getImage()).isEqualTo("TFlarp");
    assertThat(node.getLeft()).isInstanceOf(BinaryExpressionNode.class);
    assertThat(((BinaryExpressionNode) node.getLeft()).getOperator()).isEqualTo(BinaryOperator.AS);
  }

  @Test
  void testIsNotChainedWithRelationalOperator() {
    BinaryExpressionNode node = parse("Foo := Bar is not TBaz = False;");

    assertThat(node.getOperator()).isEqualTo(BinaryOperator.EQUAL);
    assertThat(node.getLeft()).isInstanceOf(BinaryExpressionNode.class);
    assertThat(((BinaryExpressionNode) node.getLeft()).getOperator())
        .isEqualTo(BinaryOperator.IS_NOT);
  }

  @Test
  void testNestedBinaryExpressionTokenRanges() {
    BinaryExpressionNode node = parse("Foo := Bar + Baz - Flarp;");

    assertThat(node.getOperator()).isEqualTo(BinaryOperator.SUBTRACT);
    assertThat(node.getFirstToken().getImage()).isEqualTo("Bar");
    assertThat(node.getLastToken().getImage()).isEqualTo("Flarp");

    BinaryExpressionNode inner = (BinaryExpressionNode) node.getLeft();
    assertThat(inner.getOperator()).isEqualTo(BinaryOperator.ADD);
    assertThat(inner.getFirstToken().getImage()).isEqualTo("Bar");
    assertThat(inner.getLastToken().getImage()).isEqualTo("Baz");
  }

  @Test
  void testNestedUnaryExpressionTokenRanges() {
    BinaryExpressionNode node = parse("Foo := not Bar in Baz;");

    UnaryExpressionNode inner = (UnaryExpressionNode) node.getLeft();
    assertThat(inner.getFirstToken().getImage()).isEqualTo("not");
    assertThat(inner.getLastToken().getImage()).isEqualTo("Bar");
  }

  @Test
  void testGreaterThanEqual() {
    BinaryExpressionNode node = parse("Foo := Bar >= Baz;");

    assertThat(node.getOperator()).isEqualTo(BinaryOperator.GREATER_THAN_EQUAL);
    assertThat(node.getOperatorNode().getTokenType()).isEqualTo(DelphiTokenType.GREATER_THAN_EQUAL);
    assertThat(node.getOperatorNode().getImage()).isEqualTo(">=");
    assertThat(node.getImage()).isEqualTo("Bar >= Baz");
  }

  @Test
  void testLessThanEqual() {
    BinaryExpressionNode node = parse("Foo := Bar <= Baz;");

    assertThat(node.getOperator()).isEqualTo(BinaryOperator.LESS_THAN_EQUAL);
    assertThat(node.getOperatorNode().getTokenType()).isEqualTo(DelphiTokenType.LESS_THAN_EQUAL);
    assertThat(node.getOperatorNode().getImage()).isEqualTo("<=");
    assertThat(node.getImage()).isEqualTo("Bar <= Baz");
  }

  private static BinaryExpressionNode parse(String statement) {
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
        .getFirstDescendantOfType(BinaryExpressionNode.class);
  }
}
