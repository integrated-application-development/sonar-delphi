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

import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

import net.sourceforge.pmd.RuleContext;

/**
 * Class for checking if class fields are private or protected, not public
 */
public class PublicFieldsRule extends DelphiRule {

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    if (node.getType() == DelphiLexer.TkNewType) {
      // encountering new type, checking for its fields
      Tree parent = node.getChild(0);
      boolean isPublic = false;
      for (int i = 0; i < parent.getChildCount(); ++i) {
        Tree child = parent.getChild(i);
        if (child.getType() == DelphiLexer.PUBLIC) {
          isPublic = true;
        } else if (isNotPublic(child.getType())) {
          isPublic = false;
        } else if (child.getType() == DelphiLexer.TkClassField && isPublic) {
          addViolation(ctx, (DelphiPMDNode) child);
        }
      }
    }
  }

  private boolean isNotPublic(int type) {
    return type == DelphiLexer.PRIVATE || type == DelphiLexer.PROTECTED || type == DelphiLexer.PROPERTY;
  }

}
