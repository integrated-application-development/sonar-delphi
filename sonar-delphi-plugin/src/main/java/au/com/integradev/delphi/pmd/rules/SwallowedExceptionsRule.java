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

import au.com.integradev.delphi.antlr.ast.node.CompoundStatementNode;
import au.com.integradev.delphi.antlr.ast.node.ElseBlockNode;
import au.com.integradev.delphi.antlr.ast.node.ExceptBlockNode;
import au.com.integradev.delphi.antlr.ast.node.ExceptItemNode;
import au.com.integradev.delphi.antlr.ast.node.StatementListNode;
import au.com.integradev.delphi.antlr.ast.node.StatementNode;
import java.util.List;
import net.sourceforge.pmd.RuleContext;

/**
 * Any case where an except block or an exception handler is empty means that any raised exception
 * is silently swallowed.
 */
public class SwallowedExceptionsRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(ExceptBlockNode exceptBlock, RuleContext data) {
    if (isEmptyExcept(exceptBlock)) {
      addViolation(data, exceptBlock);
    } else if (isEmptyElse(exceptBlock)) {
      addViolation(data, exceptBlock.getElseBlock());
    }

    return super.visit(exceptBlock, data);
  }

  @Override
  public RuleContext visit(ExceptItemNode handler, RuleContext data) {
    StatementNode statement = handler.getStatement();
    if (statement == null || isEmptyCompoundStatement(statement)) {
      addViolation(data, handler);
    }

    return super.visit(handler, data);
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
