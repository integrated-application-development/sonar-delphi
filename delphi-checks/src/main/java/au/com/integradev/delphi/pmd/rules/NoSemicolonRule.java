/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
import org.sonar.plugins.communitydelphi.api.ast.CaseItemStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExceptItemNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementListNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.communitydelphi.api.ast.Node;

public class NoSemicolonRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(StatementNode node, RuleContext data) {
    if (shouldVisit(node)) {
      DelphiNode violationNode = findViolationNode(node);

      if (violationNode != null) {
        addViolation(data, violationNode);
      }
    }

    return super.visit(node, data);
  }

  private boolean shouldVisit(DelphiNode node) {
    DelphiNode parent = node.jjtGetParent();
    return parent instanceof CaseItemStatementNode
        || parent instanceof ExceptItemNode
        || parent instanceof StatementListNode;
  }

  private DelphiNode findViolationNode(DelphiNode node) {
    Node nextNode = node.jjtGetParent().jjtGetChild(node.jjtGetChildIndex() + 1);
    if (nextNode == null || nextNode.jjtGetId() != DelphiLexer.SEMI) {
      return findNodePrecedingMissingSemicolon(node);
    }
    return null;
  }

  private DelphiNode findNodePrecedingMissingSemicolon(DelphiNode node) {
    DelphiNode lastNode = node;
    int childCount = lastNode.jjtGetNumChildren();

    while (childCount > 0) {
      lastNode = lastNode.jjtGetChild(childCount - 1);
      childCount = lastNode.jjtGetNumChildren();
    }

    return lastNode;
  }
}