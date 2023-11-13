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

import java.util.Objects;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "PascalStyleResultRule", repositoryKey = "delph")
@Rule(key = "PascalStyleResult")
public class PascalStyleResultCheck extends DelphiCheck {
  private static final String MESSAGE = "Assign to the Result variable instead.";

  @Override
  public DelphiCheckContext visit(RoutineImplementationNode routine, DelphiCheckContext context) {
    RoutineNameDeclaration routineNameDeclaration = routine.getRoutineNameDeclaration();
    if (routineNameDeclaration != null) {
      routine.findDescendantsOfType(StatementNode.class).stream()
          .filter(AssignmentStatementNode.class::isInstance)
          .map(AssignmentStatementNode.class::cast)
          .map(AssignmentStatementNode::getAssignee)
          .map(PascalStyleResultCheck::extractSimpleNameReference)
          .filter(Objects::nonNull)
          .forEach(
              assignee -> {
                if (assignee.getNameDeclaration() == routineNameDeclaration) {
                  reportIssue(context, assignee, MESSAGE);
                }
              });
    }
    return super.visit(routine, context);
  }

  private static NameReferenceNode extractSimpleNameReference(ExpressionNode node) {
    node = node.skipParentheses();
    if (node instanceof PrimaryExpressionNode && node.getChildren().size() == 1) {
      DelphiNode child = node.getChild(0);
      if (child instanceof NameReferenceNode && ((NameReferenceNode) child).nextName() == null) {
        return (NameReferenceNode) child;
      }
    }
    return null;
  }
}
