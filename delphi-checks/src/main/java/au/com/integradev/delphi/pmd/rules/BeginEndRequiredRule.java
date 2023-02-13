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

import org.sonar.plugins.communitydelphi.api.ast.CaseItemStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.CaseStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ElseBlockNode;
import org.sonar.plugins.communitydelphi.api.ast.IfStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodBodyNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementListNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.communitydelphi.api.ast.Node;

public class BeginEndRequiredRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(StatementNode statement, RuleContext data) {
    if (isMissingBeginEnd(statement)) {
      addViolation(data, statement);
    }
    return super.visit(statement, data);
  }

  @Override
  public RuleContext visit(ElseBlockNode elseBlock, RuleContext data) {
    if (!(elseBlock.jjtGetParent() instanceof CaseStatementNode)) {
      StatementListNode statementList = elseBlock.getStatementList();
      if (statementList.isEmpty()
          || statementList.getStatements().size() > 1
          || !(statementList.getStatements().get(0) instanceof CompoundStatementNode)) {
        addViolation(data, elseBlock);
      }
    }
    return super.visit(elseBlock, data);
  }

  private static boolean isMissingBeginEnd(StatementNode statement) {
    if (statement.jjtGetParent() instanceof MethodBodyNode) {
      return false;
    }

    if (statement instanceof CompoundStatementNode || statement instanceof CaseItemStatementNode) {
      return false;
    }

    Node parent = statement.jjtGetParent();

    if (parent instanceof CaseItemStatementNode) {
      return false;
    }

    if (statement instanceof IfStatementNode && parent instanceof IfStatementNode) {
      return ((IfStatementNode) parent).getElseStatement() != statement;
    }

    return !(parent instanceof StatementListNode);
  }
}
