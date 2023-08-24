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
package au.com.integradev.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.operator.BinaryOperator;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "AssignedNilCheckRule", repositoryKey = "delph")
@Rule(key = "NilComparison")
public class NilComparisonCheck extends DelphiCheck {
  private static final String MESSAGE = "Replace this nil-comparison with System.Assigned";

  @Override
  public DelphiCheckContext visit(BinaryExpressionNode expression, DelphiCheckContext context) {
    if (isViolation(expression)) {
      reportIssue(context, expression, MESSAGE);
    }
    return super.visit(expression, context);
  }

  private static boolean isVariableComparedToNil(ExpressionNode a, ExpressionNode b) {
    return a.skipParentheses().hasDescendantOfType(NameReferenceNode.class)
        && a.skipParentheses()
                .getFirstDescendantOfType(NameReferenceNode.class)
                .getLastName()
                .getNameDeclaration()
            instanceof VariableNameDeclaration
        && b.isNilLiteral();
  }

  private static boolean isViolation(BinaryExpressionNode expression) {
    BinaryOperator operator = expression.getOperator();
    ExpressionNode left = expression.getLeft();
    ExpressionNode right = expression.getRight();

    return (operator == BinaryOperator.EQUAL || operator == BinaryOperator.NOT_EQUAL)
        && (isVariableComparedToNil(left, right) || isVariableComparedToNil(right, left));
  }
}
