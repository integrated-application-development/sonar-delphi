/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Rule violation for unused function/procedure/method arguments
 */
public class UnusedArgumentsRule extends DelphiRule {

  private static final int MAX_LOOK_AHEAD = 3;
  private StringBuilder methodName;

  @Override
  public Object visit(DelphiPMDNode node, Object data) {
    if (node.getType() == DelphiLexer.PROCEDURE || node.getType() == DelphiLexer.FUNCTION) {
      Tree nameNode = node.getFirstChildWithType(DelphiLexer.TkFunctionName);
      if (nameNode != null) { // checking function name
        methodName = new StringBuilder(); // building function name
        for (int i = 0; i < nameNode.getChildCount(); ++i) {
          methodName.append(nameNode.getChild(i).getText());
        }
      }

      Tree argsNode = node.getFirstChildWithType(DelphiLexer.TkFunctionArgs);
      if (argsNode == null) {
        return data;
      }

      int lookIndex = 0;
      Tree beginNode = null; // looking for begin statement for function
      do {
        beginNode = node.getParent().getChild(node.getChildIndex() + (++lookIndex));
        if (lookIndex > MAX_LOOK_AHEAD || beginNode == null) {
          break;
        }
      } while (beginNode.getType() != DelphiLexer.BEGIN);

      if (beginNode == null || beginNode.getType() != DelphiLexer.BEGIN) {
        return data; // no begin..end for function
      }

      Map<String, Integer> args = processFunctionArgs(argsNode);
      if (args.isEmpty()) {
        return data; // no arguments
      }

      processFunctionBegin(beginNode, args);
      checkForUnusedArguments(args, data, node);

    }

    return data;
  }

  /**
   * Checks if some argument is unused, if so makes a violation
   * 
   * @param args
   *          Argument map
   * @param node
   * @param data
   */
  private void checkForUnusedArguments(Map<String, Integer> args, Object data, DelphiPMDNode node) {
    for(Map.Entry<String, Integer> entry : args.entrySet()) {
      if(entry.getValue() == 0) {
        addViolation(data, node, "Unused argument: '" + entry.getKey() + "' at " + methodName);
      }
    }
  }

  /**
   * Process begin node, to look for used arguments
   * 
   * @param beginNode
   *          Begin node
   * @param args
   *          Argument map
   */
  private void processFunctionBegin(Tree beginNode, Map<String, Integer> args) {
    for (int i = 0; i < beginNode.getChildCount(); ++i) {
      Tree child = beginNode.getChild(i);
      String key = child.getText();
      if (args.containsKey(key)) { // if we are using a argument, increase the counter
        Integer newValue = args.get(key) + 1;
        args.put(key, newValue);
      }

      if (child.getType() == DelphiLexer.BEGIN) {
        processFunctionBegin(child, args);
      }
    }
  }

  /**
   * Create argument map, and set their used count to 0
   * 
   * @param argsNode
   *          Function argument node
   * @return Argument map
   */
  private Map<String, Integer> processFunctionArgs(Tree argsNode) {
    Map<String, Integer> args = new HashMap<String, Integer>();
    for (int i = 0; i < argsNode.getChildCount(); i += 2) {
      Tree idents = argsNode.getChild(i); // TkVariableIdents node

      if (idents.getType() != DelphiLexer.TkVariableIdents) { // check type
        idents = argsNode.getChild(++i);
        if (idents == null || idents.getType() != DelphiLexer.TkVariableIdents) {
          break;
        }
      }

      for (int c = 0; c < idents.getChildCount(); ++c) { // adding arguments to map
        if ( !"sender".equals(idents.getChild(c).getText())) {
          args.put(idents.getChild(c).getText(), Integer.valueOf(0));
        }
      }
    }
    return args;
  }

}
