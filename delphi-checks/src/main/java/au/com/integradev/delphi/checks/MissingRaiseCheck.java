/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFix;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFixEdit;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineKind;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;

@Rule(key = "MissingRaise")
public class MissingRaiseCheck extends DelphiCheck {
  private static final String BASE_EXCEPTION = "System.SysUtils.Exception";

  @Override
  public DelphiCheckContext visit(
      ExpressionStatementNode expressionStatement, DelphiCheckContext context) {
    if (isExceptionConstructorInvocation(expressionStatement.getExpression())) {
      context
          .newIssue()
          .onNode(expressionStatement)
          .withMessage("Raise or delete this exception.")
          .withQuickFixes(
              QuickFix.newFix("Raise exception")
                  .withEdit(
                      QuickFixEdit.insertBefore("raise ", expressionStatement.getExpression())))
          .report();
    }

    return context;
  }

  private boolean isExceptionConstructorInvocation(ExpressionNode expression) {
    if (!(expression instanceof PrimaryExpressionNode)
        || !(expression.getChild(0) instanceof NameReferenceNode)) {
      return false;
    }

    NameDeclaration declaration =
        ((NameReferenceNode) expression.getChild(0)).getLastName().getNameDeclaration();
    if (!(declaration instanceof RoutineNameDeclaration)) {
      return false;
    }

    RoutineNameDeclaration routineDeclaration = (RoutineNameDeclaration) declaration;
    if (routineDeclaration.getRoutineKind() != RoutineKind.CONSTRUCTOR) {
      return false;
    }

    TypeNameDeclaration typeDecl = routineDeclaration.getTypeDeclaration();
    if (typeDecl == null) {
      return false;
    }

    Type type = typeDecl.getType();

    return type.is(BASE_EXCEPTION) || type.isDescendantOf(BASE_EXCEPTION);
  }
}
