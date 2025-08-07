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

import com.google.common.base.Splitter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;

@Rule(key = "ObjectPassedAsInterface")
public class ObjectPassedAsInterfaceCheck extends DelphiCheck {
  private static final String MESSAGE = "Do not pass this object reference as an interface.";

  private static final String DEFAULT_EXCLUDED_TYPES =
      "System.TNoRefCountObject,"
          + "System.Generics.Defaults.TSingletonImplementation,"
          + "System.Classes.TComponent";

  @RuleProperty(
      key = "excludedTypes",
      description =
          "Comma-delimited list of object types that this rule ignores. (case-insensitive)",
      defaultValue = DEFAULT_EXCLUDED_TYPES)
  public String excludedTypes = DEFAULT_EXCLUDED_TYPES;

  private List<String> excludedTypesList;

  @Override
  public void start(DelphiCheckContext context) {
    excludedTypesList = Splitter.on(',').trimResults().splitToList(excludedTypes);
  }

  @Override
  public DelphiCheckContext visit(ArgumentListNode argumentList, DelphiCheckContext context) {
    var interfaceIndices = getInterfaceParameterIndices(argumentList);
    var arguments = argumentList.getArgumentNodes();
    for (int i = 0; i < arguments.size(); i++) {
      if (!interfaceIndices.contains(i)) {
        continue;
      }

      ExpressionNode expression = arguments.get(i).getExpression();

      if (isObjectReferenceExpression(expression)) {
        reportIssue(context, expression, MESSAGE);
      }
    }

    return super.visit(argumentList, context);
  }

  private boolean isObjectReferenceExpression(ExpressionNode expression) {
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

    Type type = ((VariableNameDeclaration) declaration).getType();

    return type.isClass()
        && excludedTypesList.stream().noneMatch(e -> type.is(e) || type.isDescendantOf(e));
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
