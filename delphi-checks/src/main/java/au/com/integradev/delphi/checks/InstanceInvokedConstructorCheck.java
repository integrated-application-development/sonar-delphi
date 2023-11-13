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
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineKind;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Typed;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "ObjectInvokedConstructorRule", repositoryKey = "delph")
@Rule(key = "InstanceInvokedConstructor")
public class InstanceInvokedConstructorCheck extends DelphiCheck {
  private static final String MESSAGE =
      "Invoke this constructor on the type name instead of an instance.";

  @Override
  public DelphiCheckContext visit(NameReferenceNode reference, DelphiCheckContext context) {
    if (isConstructor(reference) && isInvokedOnObject(reference)) {
      reportIssue(context, reference.getIdentifier(), MESSAGE);
    }
    return super.visit(reference, context);
  }

  private static boolean isConstructor(NameReferenceNode reference) {
    NameDeclaration declaration = reference.getNameDeclaration();
    return declaration instanceof RoutineNameDeclaration
        && ((RoutineNameDeclaration) declaration).getRoutineKind() == RoutineKind.CONSTRUCTOR;
  }

  private static boolean isInvokedOnObject(NameReferenceNode reference) {
    if (reference.getFirstParentOfType(PrimaryExpressionNode.class) == null) {
      return false;
    }

    NameReferenceNode previous = reference.prevName();
    if (previous == null) {
      return false;
    }

    NameDeclaration declaration = previous.getNameDeclaration();
    return declaration instanceof VariableNameDeclaration
        && !((Typed) declaration).getType().isClassReference()
        && !((VariableNameDeclaration) declaration).isSelf();
  }
}
