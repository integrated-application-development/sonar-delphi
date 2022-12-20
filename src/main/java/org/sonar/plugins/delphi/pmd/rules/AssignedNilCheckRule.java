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
package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.BinaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.operator.BinaryOperator;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;

public class AssignedNilCheckRule extends AbstractDelphiRule {

  private boolean isRuleViolated(ExpressionNode a, ExpressionNode b) {
    return a.skipParentheses().hasDescendantOfType(NameReferenceNode.class)
        && a.skipParentheses()
                .getFirstDescendantOfType(NameReferenceNode.class)
                .getLastName()
                .getNameDeclaration()
            instanceof VariableNameDeclaration
        && b.isNilLiteral();
  }

  private boolean isEitherSideViolating(ExpressionNode left, ExpressionNode right) {
    return isRuleViolated(left, right) || isRuleViolated(right, left);
  }

  @Override
  public RuleContext visit(BinaryExpressionNode expression, RuleContext data) {
    BinaryOperator operator = expression.getOperator();
    if ((operator == BinaryOperator.EQUAL || operator == BinaryOperator.NOT_EQUAL)
        && isEitherSideViolating(expression.getLeft(), expression.getRight())) {
      addViolation(data, expression);
    }
    return super.visit(expression, data);
  }
}
