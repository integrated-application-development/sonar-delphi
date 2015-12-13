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

public class FieldNameRule extends DelphiRule {

  @Override
  protected void init() {
    super.init();
  }

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    if (node.getType() == DelphiLexer.TkClassField) {

      if (!isPublished()) {
        Tree variableIdentsNode = node.getChild(0);
        String name = variableIdentsNode.getChild(0).getText();
        if (name.length() > 1) {
	        char firstCharAfterPrefix = name.charAt(1);

	        if (!name.startsWith("F") || firstCharAfterPrefix != Character.toUpperCase(firstCharAfterPrefix)) {
	          addViolation(ctx, node);
	        }
        } else {
        	// a single letter name has no prefix 
          addViolation(ctx, node);
        }
      }
    }
  }

}
