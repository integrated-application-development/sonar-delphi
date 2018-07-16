/*
 * Sonar Delphi Plugin
 * Copyright (C) 2015 Fabricio Colombo
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

import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class InterfaceNameRule extends DelphiRule {

    /**
     * This rule looks for the interface name and if it doesn't start with "I" it raises a violation.
     *
     * @param node the current node
     * @param ctx the ruleContext to store the violations
     */
  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {

    if (node.getType() == DelphiLexer.TkInterface) {
        CommonTree interfaceNameNode = (CommonTree) node.getParent();
        /* This safely casts interfaceNameNode from CommonTree to DelphiPMDNode type.
        Since (DelphiPMDNode) <CommonTree object> casting will not work. */
        DelphiPMDNode violationNode = new DelphiPMDNode(interfaceNameNode);

        String name = interfaceNameNode.getText();

      if (!name.startsWith("I")) {
        addViolation(ctx, violationNode);
      }
    }
  }
}