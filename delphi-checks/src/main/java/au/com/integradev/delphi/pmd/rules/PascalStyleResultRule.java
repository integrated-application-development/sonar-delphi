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
package au.com.integradev.delphi.pmd.rules;

import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.ast.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import java.util.Objects;
import net.sourceforge.pmd.RuleContext;

public class PascalStyleResultRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    MethodNameDeclaration methodNameDeclaration = method.getMethodNameDeclaration();
    if (methodNameDeclaration != null) {
      method.findDescendantsOfType(StatementNode.class).stream()
          .filter(AssignmentStatementNode.class::isInstance)
          .map(AssignmentStatementNode.class::cast)
          .map(AssignmentStatementNode::getAssignee)
          .map(ExpressionNode::extractSimpleNameReference)
          .filter(Objects::nonNull)
          .forEach(
              assignee -> {
                if (assignee.getNameDeclaration() == methodNameDeclaration) {
                  addViolation(data, assignee);
                }
              });
    }
    return super.visit(method, data);
  }
}
