/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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

import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ExceptItemNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "TooLargeMethodRule", repositoryKey = "delph")
@DeprecatedRuleKey(ruleKey = "TooLargeMethod", repositoryKey = "community-delphi")
@Rule(key = "TooLargeRoutine")
public class TooLargeRoutineCheck extends DelphiCheck {
  private static final int DEFAULT_LIMIT = 100;

  @RuleProperty(
      key = "limit",
      description = "Maximum number of statements allowed in a routine.",
      defaultValue = DEFAULT_LIMIT + "")
  public int limit = DEFAULT_LIMIT;

  @Override
  public DelphiCheckContext visit(RoutineImplementationNode routine, DelphiCheckContext data) {
    long statements = countStatements(routine);

    if (statements > limit) {
      reportIssue(
          data,
          routine.getRoutineNameNode(),
          String.format(
              "%s is too large. Routine has %d statements (Limit is %d)",
              routine.simpleName(), statements, limit));
    }

    return super.visit(routine, data);
  }

  private static long countStatements(RoutineImplementationNode routine) {
    CompoundStatementNode block = routine.getStatementBlock();
    if (block != null) {
      int handlers = block.findDescendantsOfType(ExceptItemNode.class).size();
      long statements =
          block
              .descendantStatementStream()
              .filter(Predicate.not(CompoundStatementNode.class::isInstance))
              .count();

      return handlers + statements;
    }
    return 0;
  }
}
