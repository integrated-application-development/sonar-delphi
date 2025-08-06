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

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;

@Rule(key = "ObjectPassedAsInterface")
public class ObjectPassedAsInterfaceCheck extends DelphiCheck {
  private static final String MESSAGE = "Do not pass this object reference as an interface.";

  @Override
  public DelphiCheckContext visit(ArgumentListNode argumentList, DelphiCheckContext context) {
    var interfaceIndices = getInterfaceParameterIndices(argumentList);
    var arguments = argumentList.getArgumentNodes();
    for (int i = 0; i < arguments.size(); i++) {
      if (!interfaceIndices.contains(i)) {
        continue;
      }

      ExpressionNode expression = arguments.get(i).getExpression();

      if (isVariableWithClassType(expression)) {
        reportIssue(context, expression, MESSAGE);
      }
    }

    return super.visit(argumentList, context);
  }

  private static boolean isVariableWithClassType(ExpressionNode expression) {
    expression = expression.skipParentheses();

    if (!(expression instanceof PrimaryExpressionNode)) {
      return false;
    }

    var maybeName = expression.getChild(0);
    if (!(maybeName instanceof NameReferenceNode)) {
      return false;
    }

    var declaration = ((NameReferenceNode) maybeName).getLastName().getNameDeclaration();
    if (!(declaration instanceof VariableNameDeclaration)) {
      return false;
    }

    return ((VariableNameDeclaration) declaration).getType().isClass();
  }

  private static Set<Integer> getInterfaceParameterIndices(ArgumentListNode argumentList) {
    var maybeNameReference = argumentList.getParent().getChild(argumentList.getChildIndex() - 1);
    if (maybeNameReference instanceof NameReferenceNode) {
      var declaration = ((NameReferenceNode) maybeNameReference).getNameDeclaration();
      if (declaration instanceof RoutineNameDeclaration) {
        var routine = (RoutineNameDeclaration) declaration;
        var parameters = routine.getParameters();
        return IntStream.range(0, parameters.size())
            .filter(i -> parameters.get(i).getType().isInterface())
            .boxed()
            .collect(Collectors.toSet());
      }
    }

    return Collections.emptySet();
  }
}
