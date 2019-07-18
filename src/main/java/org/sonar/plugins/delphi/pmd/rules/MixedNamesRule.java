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

import com.qualinsight.plugins.sonarqube.smell.api.annotation.Smell;
import com.qualinsight.plugins.sonarqube.smell.api.model.SmellType;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Rule that checks if you are using function/variables names correctly, that is you don't misspell
 * them, example: <code>var xyz: integer; begin xyz := 1; //OK xYZ := 2; //BAD end;</code>
 *
 * @author SG0214809
 */
@Smell(
    minutes = 60,
    reason = "Won't handle function name mixing when the type is declared in the implementation.",
    type = SmellType.WRONG_LOGIC
)
public class MixedNamesRule extends DelphiRule {

  private final List<String> functionNames = new ArrayList<>();
  private final List<String> variableNames = new ArrayList<>();
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
        handleImplementation();
        break;

      case DelphiLexer.TkNewType:
        handleNewType(node);
        break;

      case DelphiLexer.TkFunctionName:
        handleFunctionName(node, ctx);
        break;

      case DelphiLexer.VAR:
        handleVar(node);
        break;

      case DelphiLexer.BEGIN:
        handleBegin(node, ctx);
        break;

      default:
        // Do nothing
    }
  }

  private void handleImplementation() {
    onInterface = false;
    typeName = "";
  }

  private void handleNewType(DelphiPMDNode node) {
    Tree typeNameNode = node.getFirstChildWithType(DelphiLexer.TkNewTypeName).getChild(0);
    typeName = typeNameNode.getText() + ".";
  }

  private void handleFunctionName(DelphiPMDNode node, RuleContext ctx) {
    if (onInterface) {
      functionNames.addAll(buildNames(node, false));
    } else {
      checkFunctionNames(node, ctx);
    }
  }

  private void handleVar(DelphiPMDNode node) {
    if (!onInterface) {
      variableNames.addAll(buildNames(node.getChild(0), true));
    }
  }

  private void handleBegin(DelphiPMDNode node, RuleContext ctx) {
    if (!onInterface) {
      checkVariableNames(node, ctx, true);
    }
  }

  /**
   * Check variable names between begin...end statements
   */
  private void checkVariableNames(DelphiPMDNode node, RuleContext ctx, boolean clear) {
    for (int i = 0; i < node.getChildCount(); ++i) {
      DelphiPMDNode child = new DelphiPMDNode((CommonTree) node.getChild(i), node.getASTTree());

      if (child.getLine() > skipToLine) {
        skipToLine = child.getLine();
      }

      if (child.getType() == DelphiLexer.BEGIN) {
        checkVariableNames(child, ctx, false);
      } else {
        String name = child.getText();
        String globalName = getGlobalName(name, variableNames);
        if (!globalName.equals(name)) {
          addViolation(ctx, child, "Avoid mixing variable names (found: '" + child.getText()
              + "' expected: '" + globalName + "').");
        }
      }
    }

    if (clear) {
      variableNames.clear();
    }
  }

  private String getGlobalName(String name, List<String> globalNames) {
    for (String globalName : globalNames) {
      if (name.equalsIgnoreCase(globalName)) {
        return globalName;
      }
    }
    return name;
  }

  /**
   * Check function names
   */
  private void checkFunctionNames(DelphiPMDNode node, RuleContext ctx) {
    List<String> currentNames = buildNames(node, false);
    for (String name : currentNames) {
      String globalName = getGlobalName(name, functionNames);
      if (!name.equals(globalName)) {
        addViolation(ctx, node, "Avoid mixing function names (found: '" + name + "' expected: '"
            + globalName + "').");
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
  private List<String> buildNames(Tree node, boolean multiply) {
    List<String> result = new ArrayList<>();

    if (node == null) {
      return result;
    }

    // if function name from new type declared
    if (node.getChildCount() == 1 && !multiply) {
      result.add(typeName + node.getChild(0).getText());
      return result;
    }

    if (multiply) {
      for (int i = 0; i < node.getChildCount(); ++i) {
        result.add(node.getChild(i).getText());
      }
    } else {
      StringBuilder name = new StringBuilder();

      for (int i = 0; i < node.getChildCount(); ++i) {
        name.append(node.getChild(i).getText());
      }

      result.add(name.toString());
    }

    return result;
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
