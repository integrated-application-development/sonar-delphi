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

import java.util.Collections;
import java.util.List;
import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

public abstract class VariableCounterRule extends DelphiRule implements NodeFinderInterface {

  @Override
  public void visit(DelphiNode node, RuleContext ctx) {
    List<DelphiNode> nodes = getVarParentNodes(node);

    if (nodes.isEmpty()) {
      return;
    }

    int variableCount = 0;

    for (DelphiNode varBlockNode : nodes) {
      variableCount += countVariables(varBlockNode);
    }

    int limit = getProperty(LIMIT);

    if (variableCount > limit) {
      addViolation(ctx, node, getViolationMessage(variableCount, limit));
    }
  }

  private int countVariables(DelphiNode node) {
    int count = 0;

    for (int i = 0; i < node.getChildCount(); ++i) {
      Tree child = node.getChild(i);
      if (child.getType() == DelphiLexer.TkVariableIdents) {
        count += child.getChildCount();
      }
    }

    return count;
  }

  private List<DelphiNode> getVarParentNodes(DelphiNode node) {
    DelphiNode singleNode = findNode(node);
    if (singleNode != null) {
      return Collections.singletonList(singleNode);
    }

    return findNodes(node);
  }

  protected abstract String getViolationMessage(int variableCount, int limit);
}
