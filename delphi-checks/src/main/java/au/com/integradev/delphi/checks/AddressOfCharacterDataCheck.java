/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
package au.com.integradev.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArrayAccessorNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.IntegerLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.UnaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.utils.ExpressionNodeUtils;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.directive.SwitchDirective.SwitchKind;
import org.sonar.plugins.communitydelphi.api.operator.UnaryOperator;

@Rule(key = "AddressOfCharacterData")
public class AddressOfCharacterDataCheck extends DelphiCheck {
  @Override
  public DelphiCheckContext visit(UnaryExpressionNode expressionNode, DelphiCheckContext context) {
    ExpressionNode operand = expressionNode.getOperand().skipParentheses();
    if (expressionNode.getOperator() == UnaryOperator.ADDRESS
        && operand.getChildren().size() == 2
        && isReferenceToString(operand.getChild(0))
        && isArrayAccessToFirstChar(context, operand.getChild(1))) {
      reportIssue(
          context,
          expressionNode,
          "Cast this string to Pointer instead of addressing the first character.");
    }

    return super.visit(expressionNode, context);
  }

  private static boolean isArrayAccessToFirstChar(DelphiCheckContext context, DelphiNode second) {
    if (!(second instanceof ArrayAccessorNode)) {
      return false;
    }
    if (second.getChildren().size() != 1) {
      return false;
    }
    return isLiteralForFirstCharIndex(context, second.getChild(0));
  }

  private static boolean isLiteralForFirstCharIndex(DelphiCheckContext context, DelphiNode inner) {
    if (!(inner instanceof ExpressionNode)) {
      return false;
    }
    IntegerLiteralNode literal = ExpressionNodeUtils.unwrapInteger((ExpressionNode) inner);
    return literal != null
        && literal.getValue().intValue() == (isZeroBasedStrings(literal, context) ? 0 : 1);
  }

  private static boolean isReferenceToString(DelphiNode node) {
    return node instanceof NameReferenceNode && ((NameReferenceNode) node).getType().isString();
  }

  private static boolean isZeroBasedStrings(Node expressionNode, DelphiCheckContext context) {
    return context
        .getCompilerSwitchRegistry()
        .isActiveSwitch(SwitchKind.ZEROBASEDSTRINGS, expressionNode.getTokenIndex());
  }
}
