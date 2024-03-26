/*
 * Sonar Delphi Plugin
 * Copyright (C) 2021 Integrated Application Development
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
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "FreeAndNilTObjectRule", repositoryKey = "delph")
@Rule(key = "FreeAndNilTObject")
public class FreeAndNilTObjectCheck extends DelphiCheck {
  @Override
  public DelphiCheckContext visit(PrimaryExpressionNode expression, DelphiCheckContext context) {
    if (isViolation(expression)) {
      reportIssue(
          context,
          expression,
          String.format(
              "Do not pass this expression of type '%s' to FreeAndNil",
              expression.getType().getImage()));
    }
    return super.visit(expression, context);
  }

  private static boolean isViolation(PrimaryExpressionNode expression) {
    if (!(expression.getChild(0) instanceof NameReferenceNode)) {
      return false;
    }

    if (!(expression.getChild(1) instanceof ArgumentListNode)) {
      return false;
    }

    NameReferenceNode reference = (NameReferenceNode) expression.getChild(0);
    if (reference.nextName() != null) {
      return false;
    }

    ArgumentListNode argumentList = (ArgumentListNode) expression.getChild(1);
    if (argumentList.getArgumentNodes().size() != 1) {
      return false;
    }

    NameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof RoutineNameDeclaration) {
      RoutineNameDeclaration routine = (RoutineNameDeclaration) declaration;
      if (routine.fullyQualifiedName().equals("System.SysUtils.FreeAndNil")) {
        ExpressionNode argument = argumentList.getArgumentNodes().get(0).getExpression();
        return !argument.getType().isUnresolved()
            && !argument.getType().isUnknown()
            && !argument.getType().isClass();
      }
    }

    return false;
  }
}
