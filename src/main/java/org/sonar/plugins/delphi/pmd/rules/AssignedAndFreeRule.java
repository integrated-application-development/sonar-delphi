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
package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import org.sonar.plugins.delphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.delphi.antlr.ast.node.BinaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.BinaryExpressionNode.BinaryOp;
import org.sonar.plugins.delphi.antlr.ast.node.CompoundStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.IfStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementNode;

public class AssignedAndFreeRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(IfStatementNode statement, RuleContext data) {
    DelphiNode violation = findViolation(statement);
    if (violation != null) {
      addViolation(data, violation);
    }
    return super.visit(statement, data);
  }

  private static DelphiNode findViolation(IfStatementNode ifStatement) {
    ExpressionNode guard = ifStatement.getGuardExpression().skipParentheses();
    String variableName = findVariableName(guard);

    if (variableName == null) {
      return null;
    }

    return findFreeStatement(ifStatement.getThenBranch(), variableName);
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
    Node method = guard.jjtGetChild(0);
    if (guard.jjtGetNumChildren() < 2 || !method.getImage().equalsIgnoreCase("Assigned")) {
      return null;
    }

    Node sibling = guard.jjtGetChild(1);
    if (sibling instanceof ArgumentListNode) {
      ArgumentListNode argumentList = (ArgumentListNode) sibling;
      if (!argumentList.getArguments().isEmpty()) {
        return argumentList.jjtGetChild(0).getImage();
      }
    }

    return null;
  }

  private static String findVariableNameForNilComparison(ExpressionNode guard) {
    BinaryExpressionNode expr = (BinaryExpressionNode) guard;
    if (expr.getOperator() == BinaryOp.NOT_EQUAL) {
      if (expr.getLeft().isNilLiteral()) {
        return expr.getRight().getImage();
      }

      if (expr.getRight().isNilLiteral()) {
        return expr.getLeft().getImage();
      }
    }

    return null;
  }

  private static DelphiNode findFreeStatement(StatementNode statement, String variableName) {
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
