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

import au.com.integradev.delphi.type.factory.TypeFactory;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.MethodScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.TypeScope;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.HelperType;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "UnusedTypesRule", repositoryKey = "delph")
@Rule(key = "UnusedType")
public class UnusedTypeCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this unused type.";

  @Override
  public DelphiCheckContext visit(TypeDeclarationNode type, DelphiCheckContext context) {
    NameDeclarationNode name = type.getTypeNameNode();
    if (canBeUnused(type.getType())
        && name.getUsages().stream()
            .allMatch(occurrence -> isWithinType(occurrence, type.getType()))) {
      reportIssue(context, name, MESSAGE);
    }
    return super.visit(type, context);
  }

  private static boolean canBeUnused(Type type) {
    return !(type instanceof HelperType);
  }

  private static boolean isWithinType(NameOccurrence occurrence, Type type) {
    DelphiScope scope = occurrence.getLocation().getScope();
    while (scope != null) {
      DelphiScope typeScope = DelphiScope.unknownScope();
      if (scope instanceof MethodScope) {
        typeScope = ((MethodScope) scope).getTypeScope();
      } else if (scope instanceof TypeScope) {
        typeScope = scope;
      }

      Type foundType = TypeFactory.unknownType();
      if (typeScope instanceof TypeScope) {
        foundType = ((TypeScope) typeScope).getType();
      }

      if (type.is(foundType)) {
        return true;
      }

      scope = scope.getParent();
    }
    return false;
  }
}
