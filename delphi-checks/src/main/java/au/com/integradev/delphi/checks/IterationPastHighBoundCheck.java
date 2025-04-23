/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ForToStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.IntegerLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFix;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFixEdit;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypedDeclaration;

@Rule(key = "IterationPastHighBound")
public class IterationPastHighBoundCheck extends DelphiCheck {
  @Override
  public DelphiCheckContext visit(ForToStatementNode forStatement, DelphiCheckContext context) {
    var target = forStatement.getTargetExpression().skipParentheses();
    var initializer = forStatement.getInitializerExpression().skipParentheses();

    if (isZero(initializer) && isSuspiciousTargetExpression(target)) {
      context
          .newIssue()
          .onNode(target)
          .withMessage("Ensure this for loop is not iterating beyond the end of the collection.")
          .withQuickFixes(
              QuickFix.newFix("Subtract one from expression")
                  .withEdit(QuickFixEdit.insertAfter(" - 1", target)))
          .report();
    }

    return super.visit(forStatement, context);
  }

  private boolean isZero(ExpressionNode expression) {
    if (!(expression instanceof PrimaryExpressionNode)) {
      return false;
    }

    var child = expression.getChild(0);
    return child instanceof IntegerLiteralNode
        && ((IntegerLiteralNode) child).getValue().intValue() == 0;
  }

  private boolean isSuspiciousTargetExpression(ExpressionNode expression) {
    if (!(expression instanceof PrimaryExpressionNode)) {
      return false;
    }

    var reference = expression.getChild(0);
    if (!(reference instanceof NameReferenceNode)) {
      return false;
    }

    var declaration = ((NameReferenceNode) reference).getLastName().getNameDeclaration();
    return isArrayLength(declaration) || isCount(declaration);
  }

  private boolean isCount(NameDeclaration declaration) {
    if (!(declaration instanceof TypedDeclaration)) {
      return false;
    }

    return declaration.getName().equalsIgnoreCase("Count")
        && ((TypedDeclaration) declaration).getType().isInteger();
  }

  private boolean isArrayLength(NameDeclaration declaration) {
    if (!(declaration instanceof RoutineNameDeclaration)) {
      return false;
    }

    var routine = (RoutineNameDeclaration) declaration;
    return routine.fullyQualifiedName().equals("System.Length")
        && routine.getParameters().get(0).getType().is("<array>");
  }
}
