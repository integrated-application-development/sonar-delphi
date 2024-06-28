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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.AsmStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFix;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFixEdit;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "LowerCaseReservedWordsRule", repositoryKey = "delph")
@Rule(key = "LowercaseKeyword")
public class LowercaseKeywordCheck extends DelphiCheck {
  @RuleProperty(
      key = "excludedKeywords",
      description = "Comma-delimited list of keywords that this rule ignores (case-insensitive).")
  public String excludedKeywords = "";

  private Set<String> excludedSet;

  @Override
  public void start(DelphiCheckContext context) {
    excludedSet =
        ImmutableSortedSet.copyOf(
            String.CASE_INSENSITIVE_ORDER, Splitter.on(',').trimResults().split(excludedKeywords));
  }

  @Override
  public DelphiCheckContext visit(DelphiNode node, DelphiCheckContext context) {
    if (isIssueNode(node)) {
      String actual = node.getToken().getImage();
      String expected = actual.toLowerCase();

      FilePosition issuePosition = FilePosition.from(node.getToken());

      context
          .newIssue()
          .onFilePosition(issuePosition)
          .withMessage("Lowercase this keyword (found: \"%s\" expected: \"%s\").", actual, expected)
          .withQuickFixes(
              QuickFix.newFix("Correct to \"%s\"", expected)
                  .withEdit(QuickFixEdit.replace(issuePosition, expected)))
          .report();
    }
    return super.visit(node, context);
  }

  @Override
  public DelphiCheckContext visit(AsmStatementNode node, DelphiCheckContext context) {
    // Do not look inside asm blocks
    return context;
  }

  private boolean isIssueNode(DelphiNode node) {
    if (!node.getToken().isKeyword()) {
      return false;
    }

    var image = node.getToken().getImage();
    return !excludedSet.contains(image) && !StringUtils.isAllLowerCase(image);
  }
}
