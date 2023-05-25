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

import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.RaiseStatementNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "RaisingGeneralExceptionRule", repositoryKey = "delph")
@Rule(key = "RaisingRawException")
public class RaisingRawExceptionCheck extends DelphiCheck {
  private static final String MESSAGE = "Raise a more specific exception type.";
  private static final Pattern EXCEPTION_CREATE = Pattern.compile("(?i)Exception.Create\\b.*");

  @Override
  public DelphiCheckContext visit(RaiseStatementNode raise, DelphiCheckContext context) {
    if (isRaisingRawException(raise)) {
      reportIssue(context, raise, MESSAGE);
    }
    return super.visit(raise, context);
  }

  private static boolean isRaisingRawException(RaiseStatementNode raise) {
    ExpressionNode expr = raise.getRaiseExpression();
    if (expr == null) {
      return false;
    }

    ExpressionNode primary = expr.skipParentheses();
    if (!(primary instanceof PrimaryExpressionNode)) {
      return false;
    }

    return EXCEPTION_CREATE.matcher(primary.getImage()).matches();
  }
}
