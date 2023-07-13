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
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementListNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "SuperfluousSemicolonsRule", repositoryKey = "delph")
@Rule(key = "SuperfluousSemicolon")
public class SuperfluousSemicolonCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this superfluous semicolon.";

  @Override
  public DelphiCheckContext visit(StatementListNode statementList, DelphiCheckContext context) {
    DelphiNode previous = null;
    for (int i = 0; i < statementList.jjtGetNumChildren(); ++i) {
      DelphiNode current = statementList.jjtGetChild(i);
      if (current.getTokenType() == DelphiTokenType.SEMICOLON
          && !(previous instanceof StatementNode)) {
        reportIssue(context, current, MESSAGE);
      }
      previous = current;
    }
    return super.visit(statementList, context);
  }
}
