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
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Rule that is checking how many arguments a function has - if too many, it
 * triggers a violation
 */
public class VariableCounter extends DelphiRule {

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    // if function arguments node
    if (node.getText().equals(getStringProperty(START))) {
      int count = 0;

      // count num of arguments
      for (int i = 0; i < node.getChildCount(); ++i) {
        Tree child = node.getChild(i);
        if (child.getType() == DelphiLexer.TkVariableIdents) {
          count += child.getChildCount();
        }
      }

      int limit = getIntProperty(LIMIT);
      if (count > limit) {
        String msg = "Too many " + getStringProperty(LOOK_FOR) + ": " + count + " (max "
          + limit + ")";
        addViolation(ctx, node, msg);
      }
    }
  }

}
