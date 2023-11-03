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

import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.utils.ExpressionNodeUtils;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;

public abstract class AbstractWithoutInheritedCheck extends DelphiCheck {

  protected abstract String getIssueMessage();

  protected final void checkViolation(MethodImplementationNode method, DelphiCheckContext context) {
    CompoundStatementNode body = method.getStatementBlock();
    if (body == null || method.isClassMethod() || hasInheritedStatement(body)) {
      return;
    }

    reportIssue(context, method.getMethodNameNode(), getIssueMessage());
  }

  private static boolean hasInheritedStatement(CompoundStatementNode body) {
    return body.descendantStatementStream()
        .filter(ExpressionStatementNode.class::isInstance)
        .map(ExpressionStatementNode.class::cast)
        .map(ExpressionStatementNode::getExpression)
        .filter(PrimaryExpressionNode.class::isInstance)
        .map(PrimaryExpressionNode.class::cast)
        .anyMatch(ExpressionNodeUtils::isInherited);
  }
}
