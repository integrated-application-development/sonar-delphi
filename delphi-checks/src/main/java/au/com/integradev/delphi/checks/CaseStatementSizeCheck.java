/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
import org.sonar.plugins.communitydelphi.api.ast.CaseStatementNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "CaseStatementSizeRule", repositoryKey = "delph")
@Rule(key = "CaseStatementSize")
public class CaseStatementSizeCheck extends DelphiCheck {
  private static final String MESSAGE = "Replace this 'case' statement with an 'if' statement.";

  @Override
  public DelphiCheckContext visit(CaseStatementNode caseStatement, DelphiCheckContext context) {
    if (caseStatement.getCaseItems().size() < 2) {
      reportIssue(context, caseStatement.getChild(0), MESSAGE);
    }

    return super.visit(caseStatement, context);
  }
}
