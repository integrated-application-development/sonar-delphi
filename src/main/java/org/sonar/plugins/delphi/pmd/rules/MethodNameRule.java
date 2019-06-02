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

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class MethodNameRule extends DelphiRule {

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {

    if (node.getType() == DelphiLexer.TkNewType && (isInterface(node) || !isPublished())) {
      List<Tree> methodNodes = node.findAllChildren(DelphiLexer.TkFunctionName);

      for (Tree method : methodNodes) {
        String name = method.getChild(0).getText();
        char firstChar = name.charAt(0);

        if (firstChar != Character.toUpperCase(firstChar)) {
          addViolation(ctx, (DelphiPMDNode) method);
        }
      }
    }

  }

  private boolean isInterface(DelphiPMDNode node) {
    return node.getChild(0).getChild(0).getType() == DelphiLexer.TkInterface;
  }

}
