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
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.UnaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.operator.UnaryOperator;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.RoutineScope;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "AddressOfNestedMethodRule", repositoryKey = "delph")
@DeprecatedRuleKey(ruleKey = "AddressOfSubroutine", repositoryKey = "community-delphi")
@Rule(key = "AddressOfNestedRoutine")
public class AddressOfNestedRoutineCheck extends DelphiCheck {
  private static final String MESSAGE =
      "Remove this procedural value referencing a nested routine.";

  @Override
  public DelphiCheckContext visit(UnaryExpressionNode expression, DelphiCheckContext context) {
    if (isAddressOfNestedRoutine(expression)) {
      reportIssue(context, expression, MESSAGE);
    }

    return super.visit(expression, context);
  }

  private static boolean isAddressOfNestedRoutine(UnaryExpressionNode expression) {
    if (expression.getOperator() != UnaryOperator.ADDRESS) {
      return false;
    }

    if (!(expression.getOperand() instanceof PrimaryExpressionNode)) {
      return false;
    }

    PrimaryExpressionNode primary = (PrimaryExpressionNode) expression.getOperand();
    if (primary.getChildren().size() != 1) {
      return false;
    }

    Node name = primary.getChild(0);
    if (!(name instanceof NameReferenceNode)) {
      return false;
    }

    NameDeclaration declaration = ((NameReferenceNode) name).getLastName().getNameDeclaration();
    if (!(declaration instanceof RoutineNameDeclaration)) {
      return false;
    }

    DelphiScope scope = declaration.getScope();
    return scope instanceof RoutineScope && scope.getParent() instanceof RoutineScope;
  }
}
