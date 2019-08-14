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

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

public abstract class NoInheritedStatementRule extends DelphiRule {

  protected void checkViolation(RuleContext ctx, DelphiNode node) {
    DelphiNode beginNode = findBeginNode(node);

    if (beginNode == null || hasInheritedStatement(beginNode)) {
      return;
    }

    addViolation(ctx, node);
  }

  private DelphiNode findBeginNode(DelphiNode node) {
    DelphiNode declSection = node.nextNode();
    if (declSection == null || declSection.getType() != DelphiLexer.TkBlockDeclSection) {
      return null;
    }

    DelphiNode beginNode = declSection.nextNode();
    if (beginNode == null || beginNode.getType() != DelphiLexer.BEGIN) {
      return null;
    }

    return beginNode;
  }

  private boolean hasInheritedStatement(DelphiNode beginNode) {
    for (int i = 0; i < beginNode.getChildCount(); i++) {
      if (beginNode.getChildType(i) == DelphiLexer.INHERITED) {
        return true;
      }
    }

    return false;
  }

  protected boolean isClassMethod(DelphiNode node) {
    DelphiNode prevNode = node.prevNode();
    return prevNode != null && prevNode.getType() == DelphiLexer.CLASS;
  }
}
