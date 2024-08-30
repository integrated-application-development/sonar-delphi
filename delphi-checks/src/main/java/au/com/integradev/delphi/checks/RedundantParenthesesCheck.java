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
import org.sonar.plugins.communitydelphi.api.ast.ParenthesizedExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFix;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFixEdit;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "RedundantParenthesesRule", repositoryKey = "delph")
@Rule(key = "RedundantParentheses")
public class RedundantParenthesesCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove these redundant parentheses.";

  @Override
  public DelphiCheckContext visit(
      ParenthesizedExpressionNode expression, DelphiCheckContext context) {
    if (expression.getExpression() instanceof ParenthesizedExpressionNode
        || expression.getExpression() instanceof PrimaryExpressionNode) {
      context
          .newIssue()
          .onNode(expression.getChild(0))
          .withMessage(MESSAGE)
          .withQuickFixes(
              QuickFix.newFix("Remove redundant parentheses")
                  .withEdits(
                      QuickFixEdit.delete(expression.getChild(0)),
                      QuickFixEdit.delete(expression.getChild(2))))
          .report();
    }
    return super.visit(expression, context);
  }
}
