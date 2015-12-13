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

import java.util.ArrayList;
import java.util.List;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

import net.sourceforge.pmd.RuleContext;

/**
 * Rule that checks if you are using function/variables names correctly, that is
 * you don't mispell them, example: <code>var
 * xyz: integer;
 * begin
 * xyz := 1;	//OK
 * xYZ := 2;	//BAD
 * end;</code>
 * 
 * @author SG0214809
 * 
 */
public class MixedNamesRule extends DelphiRule {

  private List<String> functionNames = new ArrayList<String>();
  private List<String> variableNames = new ArrayList<String>();
  private boolean onInterface = true;
  private String typeName = "";

  @Override
  public void init() {
    functionNames.clear();
    variableNames.clear();
    onInterface = true;
  }

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    int type = node.getType();
    switch (type) {
      case DelphiLexer.IMPLEMENTATION:
        onInterface = false;
        typeName = "";
        break;
      case DelphiLexer.TkNewType:
        typeName = node.getChild(0).getText() + ".";
        break;
      case DelphiLexer.TkFunctionName:
        if (onInterface) {
          functionNames.addAll(buildNames(node, false));
        } else {
          checkFunctionNames(node, ctx);
        }
        break;
      case DelphiLexer.VAR:
        if (!onInterface) {
          variableNames.addAll(buildNames(node.getChild(0), true));
        }
        break;
      case DelphiLexer.BEGIN:
        if (!onInterface) {
          checkVariableNames(node, ctx, true);
        }
        break;
    }
  }

  /**
   * Check variable names between begin...end statements
   */
  protected void checkVariableNames(DelphiPMDNode node, Object data, boolean clear) {
    for (int i = 0; i < node.getChildCount(); ++i) {

      // Cast exception was thrown, so we use c-tor instead of casting to DelphiPMDNode
      DelphiPMDNode child = new DelphiPMDNode((CommonTree) node.getChild(i));
      if (child.getLine() > lastLineParsed) {
        lastLineParsed = child.getLine();
      }
      if (child.getType() == DelphiLexer.BEGIN) {
        checkVariableNames(child, data, false);
      } else {
        for (String globalName : variableNames) {
          if (child.getText().equalsIgnoreCase(globalName.toLowerCase())
            && !child.getText().equals(globalName)) {
            addViolation(data, child, "Avoid mixing variable names (found: '" + child.getText()
              + "' expected: '" + globalName + "').");
          }
        }
      }
    }

    if (clear) {
      variableNames.clear();
    }
  }

  /**
   * Check function names
   */
  protected void checkFunctionNames(DelphiPMDNode node, Object data) {
    List<String> currentNames = buildNames(node, false);
    for (String name : currentNames) {
      for (String globalName : functionNames) {
        if (name.equalsIgnoreCase(globalName.toLowerCase()) && !name.equals(globalName)) {
          addViolation(data, node, "Avoid mixing function names (found: '" + name + "' expected: '"
            + globalName + "').");
        }
      }
    }
  }

  /**
   * Build names from current node (TkVariableIdents of TkFunctionName node)
   * 
   * @param node Node from which to build names
   * @param multiply If true, each node child will be treated as a new name
   * @return List of names
   */
  protected List<String> buildNames(Tree node, boolean multiply) {
    List<String> result = new ArrayList<String>();

    if (node == null) {
      return result;
    }
    // if function name from new type declared
    if (node.getChildCount() == 1 && !multiply) {
      result.add(typeName + node.getChild(0).getText());
      return result;
    }

    StringBuilder name = new StringBuilder();
    for (int i = 0; i < node.getChildCount(); ++i) {
      if (multiply) {
        // variable names
        result.add(node.getChild(i).getText());
      } else {
        // function name from implementation section
        name.append(node.getChild(i).getText());
      }
    }
    if (!multiply) {
      result.add(name.toString());
    }
    return result;
  }

}
