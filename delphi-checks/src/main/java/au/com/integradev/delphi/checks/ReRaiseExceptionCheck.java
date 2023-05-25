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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ExceptItemNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.RaiseStatementNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

/**
 * This rule looks for exception blocks where the caught exception is explicitly re-raised. This is
 * a Bad Thing™ because the exception will be freed at the end of the handler, causing tricky access
 * errors
 *
 * @see <a href="https://bit.ly/2AQp1GW">On Delphi Exception raising, re-raising and try-except
 *     blocks</a>
 * @see <a href="http://delphi.org/2017/06/really-bad-exception-abuse/">Exceptionally Bad Exception
 *     Abuse</a>
 */
@DeprecatedRuleKey(ruleKey = "ReRaiseExceptionRule", repositoryKey = "delph")
@Rule(key = "ReRaiseException")
public class ReRaiseExceptionCheck extends DelphiCheck {
  private static final String MESSAGE =
      "Use a bare 'raise' instead of explicitly re-raising this caught exception.";

  @Override
  public DelphiCheckContext visit(ExceptItemNode handler, DelphiCheckContext data) {
    for (NameReferenceNode raise : findViolations(handler)) {
      reportIssue(data, raise, MESSAGE);
    }
    return super.visit(handler, data);
  }

  private List<NameReferenceNode> findViolations(ExceptItemNode handler) {
    NameDeclarationNode exceptionName = handler.getExceptionName();
    if (exceptionName != null) {
      return handler.findDescendantsOfType(RaiseStatementNode.class).stream()
          .map(RaiseStatementNode::getRaiseExpression)
          .filter(Objects::nonNull)
          .map(ExpressionNode::skipParentheses)
          .filter(PrimaryExpressionNode.class::isInstance)
          .map(expr -> expr.jjtGetChild(0))
          .filter(NameReferenceNode.class::isInstance)
          .map(NameReferenceNode.class::cast)
          .filter(raised -> raised.getImage().equalsIgnoreCase(exceptionName.getImage()))
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }
}
