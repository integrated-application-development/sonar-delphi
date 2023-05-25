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

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ElseBlockNode;
import org.sonar.plugins.communitydelphi.api.ast.ExceptBlockNode;
import org.sonar.plugins.communitydelphi.api.ast.ExceptItemNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementListNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "SwallowedExceptionsRule", repositoryKey = "delph")
@Rule(key = "SwallowedException")
public class SwallowedExceptionCheck extends DelphiCheck {
  private static final String MESSAGE = "Either log or re-raise this exception.";

  @Override
  public DelphiCheckContext visit(ExceptBlockNode exceptBlock, DelphiCheckContext context) {
    if (isEmptyExcept(exceptBlock)) {
      reportIssue(context, exceptBlock, MESSAGE);
    } else if (isEmptyElse(exceptBlock)) {
      reportIssue(context, exceptBlock.getElseBlock(), MESSAGE);
    }

    return super.visit(exceptBlock, context);
  }

  @Override
  public DelphiCheckContext visit(ExceptItemNode handler, DelphiCheckContext context) {
    StatementNode statement = handler.getStatement();
    if (statement == null || isEmptyCompoundStatement(statement)) {
      reportIssue(context, handler, MESSAGE);
    }

    return super.visit(handler, context);
  }

  private static boolean isEmptyExcept(ExceptBlockNode exceptBlock) {
    StatementListNode statementList = exceptBlock.getStatementList();
    return statementList != null && statementList.isEmpty();
  }

  private static boolean isEmptyElse(ExceptBlockNode exceptBlock) {
    ElseBlockNode elseBlock = exceptBlock.getElseBlock();
    if (elseBlock == null) {
      return false;
    }

    List<StatementNode> statements = elseBlock.getStatementList().getStatements();
    if (statements.isEmpty()) {
      return true;
    } else if (statements.size() == 1) {
      StatementNode statement = statements.get(0);
      return statement instanceof CompoundStatementNode
          && ((CompoundStatementNode) statement).isEmpty();
    }

    return false;
  }

  private static boolean isEmptyCompoundStatement(StatementNode statement) {
    return statement instanceof CompoundStatementNode
        && ((CompoundStatementNode) statement).isEmpty();
  }
}
