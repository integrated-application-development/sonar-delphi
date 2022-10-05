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

import java.util.Collections;
import java.util.List;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import org.sonar.plugins.delphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.delphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.CompoundStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementNode;

public class InheritedMethodWithNoCodeRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    DelphiNode violationNode = findViolation(method);
    if (violationNode != null) {
      addViolation(data, violationNode);
    }

    return super.visit(method, data);
  }

  private static DelphiNode findViolation(MethodImplementationNode method) {
    CompoundStatementNode block = method.getStatementBlock();
    if (block == null) {
      return null;
    }

    List<StatementNode> statements = block.getStatements();
    if (statements.size() != 1) {
      return null;
    }

    StatementNode statement = statements.get(0);
    ExpressionNode expr = null;

    if (statement instanceof ExpressionStatementNode) {
      expr = ((ExpressionStatementNode) statement).getExpression();
    } else if (statement instanceof AssignmentStatementNode) {
      AssignmentStatementNode assignment = (AssignmentStatementNode) statement;
      if (assignment.getAssignee().isResult()) {
        expr = assignment.getValue();
      }
    }

    if (isInheritedCall(method, expr)) {
      return statement;
    }

    return null;
  }

  private static boolean isInheritedCall(MethodImplementationNode method, ExpressionNode expr) {
    if (!(expr instanceof PrimaryExpressionNode)) {
      return false;
    }

    PrimaryExpressionNode expression = (PrimaryExpressionNode) expr;
    if (expression.isBareInherited()) {
      return true;
    }

    if (!expression.isInheritedCall()) {
      return false;
    }

    Node reference = expression.jjtGetChild(1);
    String methodName = method.simpleName();
    if (!(reference instanceof NameReferenceNode && reference.hasImageEqualTo(methodName))) {
      return false;
    }

    Node argumentList = expression.jjtGetChild(2);
    if (argumentList != null && !(argumentList instanceof ArgumentListNode)) {
      return false;
    }

    return argumentSignaturesMatch(method, (ArgumentListNode) argumentList);
  }

  private static boolean argumentSignaturesMatch(MethodNode method, ArgumentListNode argumentList) {
    List<FormalParameterData> parameters = method.getParameters();
    List<ExpressionNode> arguments =
        (argumentList == null) ? Collections.emptyList() : argumentList.getArguments();

    if (arguments.size() != parameters.size()) {
      return false;
    }

    for (int i = 0; i < arguments.size(); ++i) {
      if (!arguments.get(i).hasImageEqualTo(parameters.get(i).getImage())) {
        return false;
      }
    }

    return true;
  }
}
