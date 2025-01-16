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

import au.com.integradev.delphi.utils.InterfaceUtils;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.AnonymousMethodNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "EmptyMethodRule", repositoryKey = "delph")
@DeprecatedRuleKey(ruleKey = "EmptyMethod", repositoryKey = "community-delphi")
@Rule(key = "EmptyRoutine")
public class EmptyRoutineCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this empty routine.";

  @Override
  public DelphiCheckContext visit(RoutineImplementationNode routine, DelphiCheckContext context) {
    if (shouldAddViolation(routine)) {
      reportIssue(context, routine.getRoutineNameNode(), MESSAGE);
    }
    return super.visit(routine, context);
  }

  @Override
  public DelphiCheckContext visit(AnonymousMethodNode anonymousMethod, DelphiCheckContext context) {
    if (shouldAddViolation(anonymousMethod)) {
      context
          .newIssue()
          .onFilePosition(FilePosition.from(anonymousMethod.getFirstToken()))
          .withMessage(MESSAGE)
          .report();
    }
    return super.visit(anonymousMethod, context);
  }

  private static boolean shouldAddViolation(RoutineImplementationNode routine) {
    if (!routine.isEmpty()) {
      return false;
    }

    DelphiNode block = routine.getBlock();
    if (block != null && block.getComments().isEmpty()) {
      // All exclusions aside, an explanatory comment is mandatory
      return true;
    }

    RoutineNameDeclaration declaration = routine.getRoutineNameDeclaration();
    if (declaration == null) {
      return true;
    }

    return !declaration.hasDirective(RoutineDirective.OVERRIDE)
        && !declaration.hasDirective(RoutineDirective.VIRTUAL)
        && !InterfaceUtils.implementsMethodOnInterface(declaration);
  }

  private static boolean shouldAddViolation(AnonymousMethodNode anonymousMethod) {
    return anonymousMethod.isEmpty() && anonymousMethod.getStatementBlock().getComments().isEmpty();
  }
}
