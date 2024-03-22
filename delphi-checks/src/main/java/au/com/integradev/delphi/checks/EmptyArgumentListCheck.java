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
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineParametersNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Typed;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "EmptyBracketsRule", repositoryKey = "delph")
@Rule(key = "EmptyArgumentList")
public class EmptyArgumentListCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this empty argument list.";
  private static final String SYSTEM_ASSIGNED_IMAGE = "System.Assigned";

  @Override
  public DelphiCheckContext visit(RoutineParametersNode parameters, DelphiCheckContext context) {
    if (parameters.isEmpty()) {
      reportIssue(context, parameters, MESSAGE);
    }
    return super.visit(parameters, context);
  }

  @Override
  public DelphiCheckContext visit(ArgumentListNode arguments, DelphiCheckContext context) {
    if (arguments.isEmpty()
        && !isExplicitArrayConstructorInvocation(arguments)
        && !isRequiredToDistinguishProceduralFromReturn(arguments)) {
      reportIssue(context, arguments, MESSAGE);
    }
    return super.visit(arguments, context);
  }

  private static boolean isExplicitArrayConstructorInvocation(ArgumentListNode arguments) {
    Node previous = arguments.getParent().getChild(arguments.getChildIndex() - 1);
    return previous instanceof NameReferenceNode
        && ((NameReferenceNode) previous).isExplicitArrayConstructorInvocation();
  }

  private static boolean isRequiredToDistinguishProceduralFromReturn(ArgumentListNode arguments) {
    return isProcVarInvocation(arguments) || isPartOfSystemAssignedArgumentExpression(arguments);
  }

  private static boolean isProcVarInvocation(ArgumentListNode arguments) {
    Node previous = arguments.getParent().getChild(arguments.getChildIndex() - 1);
    if (previous instanceof Typed) {
      Type type = ((Typed) previous).getType();
      return type.isProcedural() && !type.isRoutine();
    }
    return true;
  }

  private static boolean isPartOfSystemAssignedArgumentExpression(ArgumentListNode arguments) {
    DelphiNode parent = arguments.getParent();
    if (parent instanceof PrimaryExpressionNode) {
      DelphiNode ancestor = parent.getParent().getParent();
      if (ancestor instanceof ArgumentListNode) {
        DelphiNode prev = ancestor.getParent().getChild(ancestor.getChildIndex() - 1);
        if (prev instanceof NameReferenceNode) {
          var declaration = ((NameReferenceNode) prev).getLastName().getNameDeclaration();
          return declaration instanceof RoutineNameDeclaration
              && ((RoutineNameDeclaration) declaration)
                  .fullyQualifiedName()
                  .equals(SYSTEM_ASSIGNED_IMAGE);
        }
      }
    }
    return false;
  }
}
