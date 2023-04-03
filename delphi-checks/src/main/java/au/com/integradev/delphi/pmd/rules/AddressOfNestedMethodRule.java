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
import org.sonar.plugins.communitydelphi.api.symbol.scope.MethodScope;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.UnaryExpressionNode;
import au.com.integradev.delphi.operator.UnaryOperator;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;

public class AddressOfNestedMethodRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(UnaryExpressionNode expression, RuleContext data) {
    if (isAddressOfSubProcedure(expression)) {
      addViolation(data, expression);
    }

    return super.visit(expression, data);
  }

  private static boolean isAddressOfSubProcedure(UnaryExpressionNode expression) {
    if (expression.getOperator() != UnaryOperator.ADDRESS) {
      return false;
    }

    if (!(expression.getOperand() instanceof PrimaryExpressionNode)) {
      return false;
    }

    PrimaryExpressionNode primary = (PrimaryExpressionNode) expression.getOperand();
    if (primary.jjtGetNumChildren() != 1) {
      return false;
    }

    Node name = primary.jjtGetChild(0);
    if (!(name instanceof NameReferenceNode)) {
      return false;
    }

    NameDeclaration declaration = ((NameReferenceNode) name).getLastName().getNameDeclaration();
    if (!(declaration instanceof MethodNameDeclaration)) {
      return false;
    }

    DelphiScope scope = declaration.getScope();
    return scope instanceof MethodScope && scope.getParent() instanceof MethodScope;
  }
}