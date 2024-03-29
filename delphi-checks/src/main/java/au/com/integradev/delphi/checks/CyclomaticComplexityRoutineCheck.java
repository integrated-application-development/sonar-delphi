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

import au.com.integradev.delphi.antlr.ast.visitors.CyclomaticComplexityVisitor;
import au.com.integradev.delphi.antlr.ast.visitors.CyclomaticComplexityVisitor.Data;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.RoutineBodyNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "MethodCyclomaticComplexityRule", repositoryKey = "delph")
@DeprecatedRuleKey(ruleKey = "CyclomaticComplexityMethod", repositoryKey = "community-delphi")
@Rule(key = "CyclomaticComplexityRoutine")
public class CyclomaticComplexityRoutineCheck extends DelphiCheck {
  private static final int DEFAULT_MAX = 20;

  private static final CyclomaticComplexityVisitor CYCLOMATIC_VISITOR =
      new CyclomaticComplexityVisitor() {
        @Override
        public Data visit(RoutineBodyNode body, Data data) {
          // Skip the block declaration section so we don't count sub-procedures.
          return body.getBlock().accept(this, data);
        }
      };

  @RuleProperty(
      key = "threshold",
      description = "The maximum authorized complexity.",
      defaultValue = "" + DEFAULT_MAX)
  private int threshold = DEFAULT_MAX;

  @Override
  public DelphiCheckContext visit(RoutineImplementationNode routine, DelphiCheckContext context) {
    int complexity = CYCLOMATIC_VISITOR.visit(routine, new Data()).getComplexity();

    if (complexity > threshold) {
      reportIssue(
          context,
          routine.getRoutineNameNode(),
          String.format(
              "The Cyclomatic Complexity of this routine \"%s\" is %d which is greater than %d"
                  + " authorized.",
              routine.simpleName(), complexity, threshold));
    }

    return super.visit(routine, context);
  }
}
