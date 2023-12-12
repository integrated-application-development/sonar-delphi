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
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.SonarLintUnsupported;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.RoutineScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.TypeScope;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.HelperType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@SonarLintUnsupported
@DeprecatedRuleKey(ruleKey = "UnusedTypesRule", repositoryKey = "delph")
@Rule(key = "UnusedType")
public class UnusedTypeCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this unused type.";

  @RuleProperty(
      key = "excludeApi",
      description =
          "Exclude types declared in the interface section, "
              + "including any nested types with public visibility.")
  public boolean excludeApi = false;

  @Override
  public DelphiCheckContext visit(TypeDeclarationNode type, DelphiCheckContext context) {
    if (isViolation(type)) {
      reportIssue(context, type.getTypeNameNode(), MESSAGE);
    }
    return super.visit(type, context);
  }

  private boolean isViolation(TypeDeclarationNode node) {
    Type type = node.getType();
    if (type instanceof HelperType) {
      return false;
    }

    if (excludeApi
        && (node.isPublic() || node.isPublished())
        && node.getFirstParentOfType(InterfaceSectionNode.class) != null) {
      return false;
    }

    return node.getTypeNameNode().getUsages().stream()
        .allMatch(occurrence -> isWithinType(occurrence, type));
  }

  private static boolean isWithinType(NameOccurrence occurrence, Type type) {
    DelphiScope scope = occurrence.getLocation().getScope();
    while (scope != null) {
      DelphiScope typeScope = DelphiScope.unknownScope();
      if (scope instanceof RoutineScope) {
        typeScope = ((RoutineScope) scope).getTypeScope();
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
