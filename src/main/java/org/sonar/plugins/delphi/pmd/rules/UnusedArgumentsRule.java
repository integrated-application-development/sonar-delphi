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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.StringMultiProperty;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Rule violation for unused function/procedure/method arguments
 */
public class UnusedArgumentsRule extends DelphiRule {

  private static final int MAX_LOOK_AHEAD = 3;

  private static final StringMultiProperty EXCLUDED_ARGS = new StringMultiProperty("excluded_args",
      "The argument names to ignore", new String[]{}, 1.0f, ',');

  private final List<String> excludedArgs = new ArrayList<>();

  public UnusedArgumentsRule() {
    definePropertyDescriptor(EXCLUDED_ARGS);
  }

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    if (isMethodNode(node)) {

      Tree argsNode = node.getFirstChildWithType(DelphiLexer.TkFunctionArgs);
      if (argsNode == null) {
        return;
      }

      Map<String, Integer> args = processFunctionArgs(argsNode);
      if (args.isEmpty()) {
        // no arguments
        return;
      }

      Stack<Tree> functionNodes = new Stack<>();
      Stack<Tree> beginNodes = new Stack<>();

      ArrayList<Stack<Tree>> stacks = populateStacks(node, functionNodes, beginNodes);
      beginNodes = stacks.get(0);
      functionNodes = stacks.get(1);

      if (functionNodes.isEmpty() || beginNodes.isEmpty()) {
        return;
      }
      final StringBuilder methodName = extractMethodName(node);
      Tree beginNode = beginNodes.peek();
      processFunctionBegin(beginNode, args);
      checkForUnusedArguments(args, ctx, node, methodName.toString());
    }
  }

  private ArrayList<Stack<Tree>> populateStacks(DelphiPMDNode node, Stack<Tree> functionNodes,
      Stack<Tree> beginNodes) {

    ArrayList<Stack<Tree>> stacks = new ArrayList<>();

    for (int i = node.getChildIndex(); i < node.getParent().getChildCount(); i++) {
      Tree childNode = node.getParent().getChild(i);
      if (isMethodNode(childNode)) {
        functionNodes.push(childNode);
      }
      if (childNode.getType() == DelphiLexer.BEGIN) {
        beginNodes.push(childNode);
      }
      if (functionNodes.size() == beginNodes.size()) {
        break;
      }
    }

    stacks.add(beginNodes);
    stacks.add(functionNodes);

    return stacks;
  }

  private StringBuilder extractMethodName(DelphiPMDNode node) {
    final StringBuilder methodName = new StringBuilder();
    Tree nameNode = node.getFirstChildWithType(DelphiLexer.TkFunctionName);
    if (nameNode != null) {
      for (int i = 0; i < nameNode.getChildCount(); ++i) {
        methodName.append(nameNode.getChild(i).getText());
      }
    }
    return methodName;
  }

  private boolean isMethodNode(Tree candidateNode) {
    return candidateNode.getType() == DelphiLexer.PROCEDURE
        || candidateNode.getType() == DelphiLexer.FUNCTION;
  }

  public Tree findBeginNode(DelphiPMDNode node) {
    int lookIndex = 0;
    Tree candidateNode = null;
    // looking for begin statement for function
    do {
      candidateNode = node.getParent().getChild(node.getChildIndex() + (++lookIndex));
      if (lookIndex > MAX_LOOK_AHEAD || candidateNode == null) {
        break;
      }

    } while (candidateNode.getType() != DelphiLexer.BEGIN);

    return candidateNode;
  }

  /**
   * Checks if some argument is unused, if so makes a violation
   *
   * @param args Argument map
   * @param node PMDNode
   * @param methodName Method name
   */
  private void checkForUnusedArguments(Map<String, Integer> args, RuleContext ctx,
      DelphiPMDNode node, String methodName) {
    for (Map.Entry<String, Integer> entry : args.entrySet()) {
      if (entry.getValue() == 0 && !ignoredArg(entry.getKey())) {
        addViolation(ctx, node, "Unused argument: '" + entry.getKey() + "' at " + methodName);
      }
    }
  }

  private boolean ignoredArg(String arg) {
    return excludedArgs.contains(arg);
  }

  /**
   * Process begin node, to look for used arguments
   *
   * @param beginNode Begin node
   * @param args Argument map
   */
  private void processFunctionBegin(Tree beginNode, Map<String, Integer> args) {
    for (int i = 0; i < beginNode.getChildCount(); ++i) {
      Tree child = beginNode.getChild(i);
      String key = child.getText().toLowerCase();
      if (args.containsKey(key)) {
        // if we are using a argument, increase the counter
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
   * @param argsNode Function argument node
   * @return Argument map
   */
  private Map<String, Integer> processFunctionArgs(Tree argsNode) {
    Map<String, Integer> args = new HashMap<>();
    for (int i = 0; i < argsNode.getChildCount(); i += 2) {
      Tree idents = argsNode.getChild(i);

      if (idents.getType() != DelphiLexer.TkVariableIdents) {
        idents = argsNode.getChild(++i);
        if (idents == null || idents.getType() != DelphiLexer.TkVariableIdents) {
          break;
        }
      }

      for (int c = 0; c < idents.getChildCount(); ++c) {
        args.put(idents.getChild(c).getText().toLowerCase(), 0);
      }
    }
    return args;
  }

  @Override
  // Code smell not to override equals in this class
  public boolean equals(Object object) {
    return object == this;
  }

  @Override
  protected void init() {
    super.init();
    List<String> stringProperties = getProperty(EXCLUDED_ARGS);
    for (String prop : stringProperties) {
      excludedArgs.add(prop.toLowerCase());
    }
  }
}
