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

import java.util.HashSet;
import java.util.Set;
import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Class for checking if we are using .Free with checking if variable is
 * assigned (redundant): if assigned(x) then x.free; if x &lt;&gt; nil then x.free;
 */
public class AssignedAndFreeRule extends DelphiRule {

  private static final int MIN_CHILD_COUNT = 5;
  protected boolean started;
  protected Set<String> variables;

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    if (node.getType() == DelphiLexer.IF) {
      started = true;
      variables.clear();
    } else if (node.getType() == DelphiLexer.THEN) {
      started = false;
    }

    // looking for variables that were checked for assignement
    if (started) {
      if ("assigned".equalsIgnoreCase(node.getText())) {
        parseAssigned(node);
      } else if ("nil".equals(node.getText())) {
        parseNil(node);
      }
    } else {
      // checking for Free'd variables
      if (node.getType() == DelphiLexer.BEGIN) {
        if (node.getChildCount() > MIN_CHILD_COUNT) {
          variables.clear();
        }
      }

      else if ("free".equalsIgnoreCase(node.getText()) && freeVariable(node)) {
        addViolation(ctx, node);
      }
    }
  }

  private boolean freeVariable(DelphiPMDNode node) {
    StringBuilder variableName = new StringBuilder();
    int index = node.getChildIndex();
    Tree backwardNode;
    while (((--index) > -1) && (backwardNode = node.getParent().getChild(index)).getText().equals(".")) {
      variableName.insert(0, backwardNode.getText());
      variableName.insert(0, node.getParent().getChild(--index).getText());
    }

    // if some variable name was found
    if (variableName.length() > 0) {
      // terminate last .
      variableName.setLength(variableName.length() - 1);
      return variables.contains(variableName.toString());
    } else {
      return false;
    }
  }

  private void parseNil(DelphiPMDNode node) {
    int index = node.getChildIndex();
    if (!node.getParent().getChild(--index).getText().equals("<>")) {
      return;
    }

    StringBuilder variableName = new StringBuilder();
    Tree backwardNode = node.getParent().getChild(--index);
    variableName.insert(0, backwardNode.getText());

    while ((backwardNode = node.getParent().getChild(--index)).getText().equals(".")) {
      variableName.insert(0, backwardNode.getText());
      variableName.insert(0, node.getParent().getChild(--index).getText());
    }

    variables.add(variableName.toString());
  }

  private void parseAssigned(DelphiPMDNode node) {
    StringBuilder variableName = new StringBuilder();
    int index = node.getChildIndex() + 1;
    Tree forwardNode;
    while (!(forwardNode = node.getParent().getChild(++index)).getText().equals(")")) {
      variableName.append(forwardNode.getText());
    }

    variables.add(variableName.toString());
  }

  @Override
  protected void init() {
    started = false;
    variables = new HashSet<String>();
  }

}
