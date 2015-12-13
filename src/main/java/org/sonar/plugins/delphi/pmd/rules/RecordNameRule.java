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

import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

import net.sourceforge.pmd.RuleContext;

public class RecordNameRule extends DelphiRule {

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {

    if (node.getType() == DelphiLexer.TkNewType) {
      Tree candidateNode = node.getChild(0).getChild(0);

      if (candidateNode.getType() == DelphiLexer.RECORD) {
        String name = candidateNode.getParent().getText();

        char firstCharAfterPrefix = name.charAt(1);

        if (!name.startsWith("T") || firstCharAfterPrefix != Character.toUpperCase(firstCharAfterPrefix)) {
          addViolation(ctx, (DelphiPMDNode) candidateNode);
        }
      }
    }
  }
}
