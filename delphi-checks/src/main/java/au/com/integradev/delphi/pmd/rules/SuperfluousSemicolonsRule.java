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

import au.com.integradev.delphi.antlr.DelphiLexer;
import au.com.integradev.delphi.antlr.ast.node.StatementListNode;
import au.com.integradev.delphi.antlr.ast.node.StatementNode;
import net.sourceforge.pmd.RuleContext;
import au.com.integradev.delphi.antlr.ast.node.Node;

public class SuperfluousSemicolonsRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(StatementListNode statementList, RuleContext data) {
    Node previous = null;
    for (int i = 0; i < statementList.jjtGetNumChildren(); ++i) {
      Node current = statementList.jjtGetChild(i);
      if (current.jjtGetId() == DelphiLexer.SEMI && !(previous instanceof StatementNode)) {
        addViolation(data, current);
      }
      previous = current;
    }
    return super.visit(statementList, data);
  }
}
