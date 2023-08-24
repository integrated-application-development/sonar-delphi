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
import org.sonar.plugins.communitydelphi.api.ast.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "MethodNestingDepthRule", repositoryKey = "delph")
@Rule(key = "MethodNestingDepth")
public class MethodNestingDepthCheck extends DelphiCheck {
  private static final int DEFAULT_DEPTH = 1;

  @RuleProperty(
      key = "depth",
      description = "The maximum nesting level allowed for a nested method.",
      defaultValue = DEFAULT_DEPTH + "")
  public int depth = DEFAULT_DEPTH;

  @Override
  public DelphiCheckContext visit(MethodImplementationNode method, DelphiCheckContext context) {
    int actualDepth = method.getParentsOfType(MethodImplementationNode.class).size();

    if (actualDepth > depth) {
      reportIssue(
          context,
          method.getMethodNameNode(),
          String.format(
              "Extract this deeply nested method. Nesting level is %d. (Limit is %d)",
              actualDepth, depth));
    }

    return super.visit(method, context);
  }
}
