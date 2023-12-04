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

import static au.com.integradev.delphi.utils.RoutineUtils.isStubRoutineWithStackUnwinding;

import au.com.integradev.delphi.utils.InterfaceUtils;
import java.util.HashSet;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.RoutineDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.SonarLintUnsupported;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.RoutineScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.UnitScope;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@SonarLintUnsupported
@DeprecatedRuleKey(ruleKey = "UnusedMethodsRule", repositoryKey = "delph")
@DeprecatedRuleKey(ruleKey = "UnusedMethod", repositoryKey = "community-delphi")
@Rule(key = "UnusedRoutine")
public class UnusedRoutineCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this unused routine.";

  private final Set<RoutineNameDeclaration> seenRoutines = new HashSet<>();

  @Override
  public DelphiCheckContext visit(DelphiAst ast, DelphiCheckContext context) {
    seenRoutines.clear();
    return super.visit(ast, context);
  }

  @Override
  public DelphiCheckContext visit(RoutineNode routine, DelphiCheckContext context) {
    RoutineNameDeclaration routineDeclaration = routine.getRoutineNameDeclaration();

    if (!seenRoutines.contains(routineDeclaration) && isViolation(routine)) {
      reportIssue(context, routine.getRoutineNameNode(), MESSAGE);
    }

    seenRoutines.add(routineDeclaration);

    return super.visit(routine, context);
  }

  private static boolean isViolation(RoutineNode routine) {
    if (routine.isPublished()) {
      return false;
    }

    RoutineNameDeclaration routineDeclaration = routine.getRoutineNameDeclaration();
    if (routineDeclaration == null) {
      return false;
    }

    if (routineDeclaration.hasDirective(RoutineDirective.OVERRIDE)) {
      return false;
    }

    if (routineDeclaration.hasDirective(RoutineDirective.MESSAGE)) {
      return false;
    }

    if (!routineDeclaration.isCallable()) {
      return false;
    }

    if (routineDeclaration.getTypeDeclaration() != null
        && !routineDeclaration.getAttributeTypes().isEmpty()) {
      return false;
    }

    if (routineDeclaration.getName().equalsIgnoreCase("Register")
        && routineDeclaration.getScope() instanceof UnitScope) {
      return false;
    }

    if (InterfaceUtils.implementsMethodOnInterface(routineDeclaration)) {
      return false;
    }

    if (isForbiddenConstructor(routine)) {
      return false;
    }

    return routine.getRoutineNameNode().getUsages().stream()
        .allMatch(occurrence -> isWithinRoutine(occurrence, routineDeclaration));
  }

  private static boolean isForbiddenConstructor(RoutineNode routine) {
    if (routine instanceof RoutineDeclarationNode && routine.isConstructor()) {
      DelphiAst ast = routine.getAst();
      return ast.findDescendantsOfType(RoutineImplementationNode.class).stream()
          .anyMatch(
              implementation -> {
                RoutineNameDeclaration declaration = implementation.getRoutineNameDeclaration();
                return declaration != null
                    && declaration.equals(routine.getRoutineNameDeclaration())
                    && isStubRoutineWithStackUnwinding(implementation);
              });
    }
    return false;
  }

  private static boolean isWithinRoutine(
      NameOccurrence occurrence, RoutineNameDeclaration routine) {
    DelphiScope scope = occurrence.getLocation().getScope();
    while (scope != null) {
      if (scope instanceof RoutineScope) {
        RoutineScope routineScope = (RoutineScope) scope;
        if (routine.equals(routineScope.getRoutineNameDeclaration())) {
          return true;
        }
      }
      scope = scope.getParent();
    }
    return false;
  }
}
