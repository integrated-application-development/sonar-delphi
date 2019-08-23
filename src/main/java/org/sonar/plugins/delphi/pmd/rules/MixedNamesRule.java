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
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

/**
 * Rule that checks if you are using function/variables names correctly, that is you don't misspell
 * them, example: <code>var xyz: integer; begin xyz := 1; //OK xYZ := 2; //BAD end;</code>
 *
 * @author SG0214809
 */
@Smell(
    minutes = 60,
    reason =
        "Won't handle function name mixing when the type is declared in the implementation."
            + "Also doesn't handle argument names.",
    type = SmellType.WRONG_LOGIC)
public class MixedNamesRule extends DelphiRule {

  private final List<String> functionNames = new ArrayList<>();
  private final List<String> variableNames = new ArrayList<>();
  private String typeName = "";

  @Override
  public void start(RuleContext ctx) {
    functionNames.clear();
    variableNames.clear();
  }

  @Override
  public void visit(DelphiNode node, RuleContext ctx) {
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

      default:
        if (isBlockNode(node)) {
          handleBlock(node, ctx);
        }
    }
  }

  private void handleImplementation() {
    typeName = "";
  }

  private void handleNewType(DelphiNode node) {
    Tree typeNameNode = node.getFirstChildWithType(DelphiLexer.TkNewTypeName).getChild(0);
    typeName = typeNameNode.getText() + ".";
  }

  private void handleFunctionName(DelphiNode node, RuleContext ctx) {
    if (isInterfaceSection()) {
      functionNames.addAll(buildNames(node, false));
    } else {
      checkFunctionNames(node, ctx);
    }
  }

  private void handleVar(DelphiNode node) {
    if (isImplementationSection()) {
      variableNames.addAll(buildNames(node.getChild(0), true));
    }
  }

  private void handleBlock(DelphiNode node, RuleContext ctx) {
    if (isImplementationSection()) {
      checkVariableNames(node, ctx, true);
    }
  }

  /** Check variable names between begin...end statements */
  private void checkVariableNames(DelphiNode node, RuleContext ctx, boolean clear) {
    for (int i = 0; i < node.getChildCount(); ++i) {
      DelphiNode child = (DelphiNode) node.getChild(i);

      if (isBlockNode(child)) {
        checkVariableNames(child, ctx, false);
        continue;
      }

      if (isUnqualifiedIdentifier(child)) {
        String name = child.getText();
        String globalName = getGlobalName(name, variableNames);
        if (!globalName.equals(name)) {
          addViolation(
              ctx,
              child,
              "Avoid mixing variable names (found: '"
                  + child.getText()
                  + "' expected: '"
                  + globalName
                  + "').");
        }
      }
    }

    if (clear) {
      variableNames.clear();
    }
  }

  private boolean isUnqualifiedIdentifier(DelphiNode node) {
    if (node.getType() == DelphiLexer.TkIdentifier) {
      DelphiNode prevNode = node.prevNode();
      return prevNode == null || prevNode.getType() != DelphiLexer.DOT;
    }

    return false;
  }

  private boolean isBlockNode(Tree node) {
    return node.getType() == DelphiLexer.BEGIN
        || node.getType() == DelphiLexer.ASM
        || node.getType() == DelphiLexer.TkAssemblerInstructions;
  }

  private String getGlobalName(String name, List<String> globalNames) {
    for (String globalName : globalNames) {
      if (name.equalsIgnoreCase(globalName)) {
        return globalName;
      }
    }
    return name;
  }

  /** Check function names */
  private void checkFunctionNames(DelphiNode node, RuleContext ctx) {
    List<String> currentNames = buildNames(node, false);
    for (String name : currentNames) {
      String globalName = getGlobalName(name, functionNames);
      if (!name.equals(globalName)) {
        addViolation(
            ctx,
            node,
            "Avoid mixing function names (found: '" + name + "' expected: '" + globalName + "').");
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
