/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package org.sonar.plugins.communitydelphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import org.sonar.plugins.communitydelphi.antlr.ast.node.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.UnaryExpressionNode;
import org.sonar.plugins.communitydelphi.operator.BinaryOperator;
import org.sonar.plugins.communitydelphi.operator.UnaryOperator;
import org.sonar.plugins.communitydelphi.type.Type;

public class RedundantBooleanRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(PrimaryExpressionNode bool, RuleContext data) {
    if (bool.isBooleanLiteral() && (isRedundantComparison(bool) || isNeedlesslyInverted(bool))) {
      addViolation(data, bool);
    }

    return super.visit(bool, data);
  }

  private static boolean isRedundantComparison(PrimaryExpressionNode bool) {
    Node parent = bool.findParentheses().jjtGetParent();

    if (parent instanceof BinaryExpressionNode) {
      BinaryExpressionNode expression = (BinaryExpressionNode) parent;

      Type leftType = expression.getLeft().getType();
      Type rightType = expression.getRight().getType();
      BinaryOperator operator = expression.getOperator();

      return leftType.isBoolean()
          && rightType.isBoolean()
          && (operator == BinaryOperator.EQUAL || operator == BinaryOperator.NOT_EQUAL);
    }

    return false;
  }

  private static boolean isNeedlesslyInverted(PrimaryExpressionNode bool) {
    Node parent = bool.findParentheses().jjtGetParent();
    return parent instanceof UnaryExpressionNode
        && ((UnaryExpressionNode) parent).getOperator() == UnaryOperator.NOT;
  }
}
