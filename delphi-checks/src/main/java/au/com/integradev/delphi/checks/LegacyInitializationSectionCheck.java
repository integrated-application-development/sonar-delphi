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
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFix;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFixEdit;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "LegacyInitializationSectionRule", repositoryKey = "delph")
@Rule(key = "LegacyInitializationSection")
public class LegacyInitializationSectionCheck extends DelphiCheck {
  private static final String MESSAGE = "Change this to an initialization section.";

  @Override
  public DelphiCheckContext visit(DelphiAst ast, DelphiCheckContext context) {
    if (ast.isUnit()) {
      DelphiNode compoundStatement = ast.getFirstChildOfType(CompoundStatementNode.class);
      if (compoundStatement != null) {
        var beginPosition = FilePosition.from(compoundStatement.getFirstToken());
        context
            .newIssue()
            .onFilePosition(beginPosition)
            .withMessage(MESSAGE)
            .withQuickFixes(
                QuickFix.newFix("Convert to initialization section")
                    .withEdit(QuickFixEdit.replace(beginPosition, "initialization")))
            .report();
      }
    }
    return context;
  }
}
