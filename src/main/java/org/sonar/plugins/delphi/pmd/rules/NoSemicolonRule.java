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
package org.sonar.plugins.delphi.pmd.rules;

import java.util.Set;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.node.CaseItemStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExceptItemNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementListNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementNode;

public class NoSemicolonRule extends AbstractDelphiRule {

  private static final Set<Class<?>> VALID_PARENTS =
      Set.of(CaseItemStatementNode.class, ExceptItemNode.class, StatementListNode.class);

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
    return VALID_PARENTS.contains(node.jjtGetParent().getClass());
  }

  private DelphiNode findViolationNode(DelphiNode node) {
    Node nextNode = node.nextNode();
    if (nextNode == null || nextNode.jjtGetId() != DelphiLexer.SEMI) {
      return findNodePrecedingMissingSemicolon(node);
    }
    return null;
  }

  private DelphiNode findNodePrecedingMissingSemicolon(DelphiNode node) {
    Node lastNode = node;
    int childCount = lastNode.jjtGetNumChildren();

    while (childCount > 0) {
      lastNode = lastNode.jjtGetChild(childCount - 1);
      childCount = lastNode.jjtGetNumChildren();
    }

    return (DelphiNode) lastNode;
  }
}
