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
package au.com.integradev.delphi.checks;

import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.IfStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.ast.utils.ExpressionNodeUtils;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.operator.BinaryOperator;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "AssignedAndFreeRule", repositoryKey = "delph")
@Rule(key = "AssignedAndFree")
public class AssignedAndFreeCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this unnecessary assignment check.";

  @Override
  public DelphiCheckContext visit(IfStatementNode statement, DelphiCheckContext context) {
    DelphiNode violation = findViolation(statement);
    if (violation != null) {
      reportIssue(context, violation, MESSAGE);
    }
    return super.visit(statement, context);
  }

  private static DelphiNode findViolation(IfStatementNode ifStatement) {
    ExpressionNode guard = ifStatement.getGuardExpression().skipParentheses();
    String variableName = findVariableName(guard);

    if (variableName == null) {
      return null;
    }

    return findFreeStatement(ifStatement.getThenStatement(), variableName);
  }

  private static String findVariableName(ExpressionNode guard) {
    if (guard instanceof PrimaryExpressionNode) {
      return findVariableNameForAssigned(guard);
    } else if (guard instanceof BinaryExpressionNode) {
      return findVariableNameForNilComparison(guard);
    }

    return null;
  }

  private static String findVariableNameForAssigned(ExpressionNode guard) {
    Node routine = guard.getChild(0);
    if (guard.getChildren().size() < 2 || !routine.getImage().equalsIgnoreCase("Assigned")) {
      return null;
    }

    Node sibling = guard.getChild(1);
    if (sibling instanceof ArgumentListNode) {
      ArgumentListNode argumentList = (ArgumentListNode) sibling;
      if (!argumentList.isEmpty()) {
        return argumentList.getArgumentNodes().get(0).getExpression().getImage();
      }
    }

    return null;
  }

  private static String findVariableNameForNilComparison(ExpressionNode guard) {
    BinaryExpressionNode expr = (BinaryExpressionNode) guard;
    if (expr.getOperator() == BinaryOperator.NOT_EQUAL) {
      if (ExpressionNodeUtils.isNilLiteral(expr.getLeft())) {
        return expr.getRight().getImage();
      }

      if (ExpressionNodeUtils.isNilLiteral(expr.getRight())) {
        return expr.getLeft().getImage();
      }
    }

    return null;
  }

  private static DelphiNode findFreeStatement(
      @Nullable StatementNode statement, String variableName) {
    if (statement instanceof CompoundStatementNode) {
      CompoundStatementNode beginStatement = (CompoundStatementNode) statement;
      if (!beginStatement.isEmpty()) {
        return findFreeStatement(beginStatement.getStatements().get(0), variableName);
      }
    } else if (statement instanceof ExpressionStatementNode) {
      ExpressionNode expr = ((ExpressionStatementNode) statement).getExpression();
      if (isFreeExpression(expr, variableName)) {
        return statement;
      }
    }
    return null;
  }

  private static boolean isFreeExpression(ExpressionNode expr, String variableName) {
    return expr.getImage().equalsIgnoreCase("FreeAndNil(" + variableName + ")")
        || expr.getImage().equalsIgnoreCase(variableName + ".Free")
        || expr.getImage().equalsIgnoreCase(variableName + ".Free()");
  }
}
