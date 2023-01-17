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
package au.com.integradev.delphi.pmd.rules;

import au.com.integradev.delphi.antlr.ast.node.ExpressionNode;
import au.com.integradev.delphi.antlr.ast.node.PrimaryExpressionNode;
import au.com.integradev.delphi.antlr.ast.node.RaiseStatementNode;
import java.util.regex.Pattern;
import net.sourceforge.pmd.RuleContext;

public class RaisingGeneralExceptionRule extends AbstractDelphiRule {

  private static final Pattern EXCEPTION_CREATE = Pattern.compile("(?i)Exception.Create\\b.*");

  @Override
  public RuleContext visit(RaiseStatementNode raise, RuleContext data) {
    if (isRaisingGeneralException(raise)) {
      addViolation(data, raise);
    }
    return super.visit(raise, data);
  }

  private static boolean isRaisingGeneralException(RaiseStatementNode raise) {
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
