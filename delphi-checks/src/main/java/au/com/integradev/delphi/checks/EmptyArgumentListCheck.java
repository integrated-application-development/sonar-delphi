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
package au.com.integradev.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodParametersNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Typed;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "EmptyBracketsRule", repositoryKey = "delph")
@Rule(key = "EmptyArgumentList")
public class EmptyArgumentListCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this empty argument list.";
  private static final String SYSTEM_ASSIGNED_IMAGE = "System.Assigned";

  @Override
  public DelphiCheckContext visit(MethodParametersNode parameters, DelphiCheckContext context) {
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
    Node previous = arguments.jjtGetParent().jjtGetChild(arguments.jjtGetChildIndex() - 1);
    return previous instanceof NameReferenceNode
        && ((NameReferenceNode) previous).isExplicitArrayConstructorInvocation();
  }

  private static boolean isRequiredToDistinguishProceduralFromReturn(ArgumentListNode arguments) {
    return isProcVarInvocation(arguments) || isPartOfSystemAssignedArgumentExpression(arguments);
  }

  private static boolean isProcVarInvocation(ArgumentListNode arguments) {
    Node previous = arguments.jjtGetParent().jjtGetChild(arguments.jjtGetChildIndex() - 1);
    if (previous instanceof Typed) {
      Type type = ((Typed) previous).getType();
      return type.isProcedural() && !type.isMethod();
    }
    return true;
  }

  private static boolean isPartOfSystemAssignedArgumentExpression(ArgumentListNode arguments) {
    DelphiNode parent = arguments.jjtGetParent();
    if (parent instanceof PrimaryExpressionNode) {
      DelphiNode grandparent = parent.jjtGetParent();
      if (grandparent instanceof ArgumentListNode) {
        DelphiNode prev =
            grandparent.jjtGetParent().jjtGetChild(grandparent.jjtGetChildIndex() - 1);
        if (prev instanceof NameReferenceNode) {
          var declaration = ((NameReferenceNode) prev).getLastName().getNameDeclaration();
          return declaration instanceof MethodNameDeclaration
              && ((MethodNameDeclaration) declaration)
                  .fullyQualifiedName()
                  .equals(SYSTEM_ASSIGNED_IMAGE);
        }
      }
    }
    return false;
  }
}
