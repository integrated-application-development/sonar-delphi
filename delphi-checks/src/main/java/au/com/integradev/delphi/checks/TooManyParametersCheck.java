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
import org.sonar.plugins.communitydelphi.api.ast.RoutineDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineNameNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "TooManyArgumentsRule", repositoryKey = "delph")
@Rule(key = "TooManyParameters")
public class TooManyParametersCheck extends DelphiCheck {
  private static final int DEFAULT_MAXIMUM = 7;

  @RuleProperty(
      key = "max",
      description = "Maximum authorized number of parameters",
      defaultValue = "" + DEFAULT_MAXIMUM)
  public int max = DEFAULT_MAXIMUM;

  @RuleProperty(
      key = "constructorMax",
      description = "Maximum authorized number of parameters for a constructor",
      defaultValue = "" + DEFAULT_MAXIMUM)
  public int constructorMax = DEFAULT_MAXIMUM;

  @Override
  public DelphiCheckContext visit(RoutineDeclarationNode routine, DelphiCheckContext context) {
    checkRoutine(
        routine.getRoutineNameNode(),
        routine.getParameters().size(),
        routine.isConstructor(),
        context);
    return super.visit(routine, context);
  }

  @Override
  public DelphiCheckContext visit(RoutineImplementationNode routine, DelphiCheckContext context) {
    var declaration = routine.getRoutineNameDeclaration();
    if (declaration == null || !declaration.isImplementationDeclaration()) {
      // Don't duplicate issues between the declaration and implementation
      return super.visit(routine, context);
    }

    // A routine implementation's default parameters MUST match its declaration's, or not have any
    // default parameters at all. This means that we need to check the declaration to get the
    // authoritative number of default parameters.
    checkRoutine(
        routine.getRoutineNameNode(),
        declaration.getParameters().size(),
        routine.isConstructor(),
        context);
    return super.visit(routine, context);
  }

  private void checkRoutine(
      RoutineNameNode node, int count, boolean isConstructor, DelphiCheckContext context) {
    var thisMax = isConstructor ? constructorMax : max;
    if (count > thisMax) {
      reportIssue(
          context,
          node,
          String.format(
              "%s has %d parameters, which is greater than %d authorized.",
              isConstructor ? "Constructor" : "Routine", count, thisMax));
    }
  }
}
