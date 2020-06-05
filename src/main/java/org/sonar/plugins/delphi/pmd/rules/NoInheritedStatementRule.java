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

import org.sonar.plugins.delphi.antlr.ast.node.CompoundStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;

public abstract class NoInheritedStatementRule extends AbstractDelphiRule {

  protected final void checkViolation(MethodImplementationNode method, Object data) {
    CompoundStatementNode body = method.getStatementBlock();
    if (body == null || method.isClassMethod() || hasInheritedStatement(body)) {
      return;
    }

    addViolation(data, method.getMethodNameNode());
  }

  private boolean hasInheritedStatement(CompoundStatementNode body) {
    return body.descendantStatementStream()
        .filter(ExpressionStatementNode.class::isInstance)
        .map(ExpressionStatementNode.class::cast)
        .map(ExpressionStatementNode::getExpression)
        .filter(PrimaryExpressionNode.class::isInstance)
        .map(PrimaryExpressionNode.class::cast)
        .anyMatch(PrimaryExpressionNode::isInheritedCall);
  }
}
