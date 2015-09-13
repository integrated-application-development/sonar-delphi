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

public class FieldNameRule extends DelphiRule {

  private int currentVisibility;

  @Override
  protected void init() {
    super.init();
    currentVisibility = -1;
  }

  @Override
  public Object visit(DelphiPMDNode node, Object data) {
    if (node.getType() == DelphiLexer.TkClassField) {
      currentVisibility = getVisibility(node);

      if ((currentVisibility == DelphiLexer.PRIVATE) || (currentVisibility == DelphiLexer.PROTECTED)) {
        Tree variableIdentsNode = node.getChild(0);
        String name = variableIdentsNode.getChild(0).getText();
        char firstCharAfterPrefix = name.charAt(1);

        if (!name.startsWith("F") || firstCharAfterPrefix != Character.toUpperCase(firstCharAfterPrefix)) {
          addViolation(data, node);
        }
      }
    }

    return data;
  }

  private int getVisibility(DelphiPMDNode node) {
    Tree siblingNode = node.getParent().getChild(node.childIndex - 1);

    switch (siblingNode.getType()) {
      case DelphiLexer.PRIVATE:
      case DelphiLexer.PROTECTED:
      case DelphiLexer.PUBLIC:
      case DelphiLexer.PUBLISHED:
        return siblingNode.getType();
      default:
        return currentVisibility;
    }
  }

}
