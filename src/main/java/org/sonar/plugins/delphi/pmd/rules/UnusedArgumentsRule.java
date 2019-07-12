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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Rule violation for unused function/procedure/method arguments
 */
public class UnusedArgumentsRule extends DelphiRule {

  private static final int MAX_LOOK_AHEAD = 3;

  private static final PropertyDescriptor<List<String>> EXCLUDED_ARGS =
      PropertyFactory.stringListProperty("excluded_args")
          .desc("The argument names to ignore")
          .build();

  private final HashSet<String> excludedArgs = new HashSet<>();

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

      Deque<Tree> functionNodes = new ArrayDeque<>();
      Deque<Tree> beginNodes = new ArrayDeque<>();

      findFunctionNodesAndBeginNodes(node, functionNodes, beginNodes);

      if (functionNodes.isEmpty() || beginNodes.isEmpty()) {
        return;
      }

      Tree beginNode = beginNodes.peek();

      processFunctionBegin(beginNode, args);
      checkForUnusedArguments(args, ctx, node, extractMethodName(node));
    }
  }

  private void findFunctionNodesAndBeginNodes(DelphiPMDNode node, Deque<Tree> functionNodes,
      Deque<Tree> beginNodes) {

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
  }

  private String extractMethodName(DelphiPMDNode node) {
    final StringBuilder methodName = new StringBuilder();
    Tree nameNode = node.getFirstChildWithType(DelphiLexer.TkFunctionName);
    if (nameNode != null) {
      for (int i = 0; i < nameNode.getChildCount(); ++i) {
        methodName.append(nameNode.getChild(i).getText());
      }
    }
    return methodName.toString();
  }

  private boolean isMethodNode(Tree candidateNode) {
    return candidateNode.getType() == DelphiLexer.PROCEDURE
        || candidateNode.getType() == DelphiLexer.FUNCTION;
  }

  public Tree findBeginNode(Tree node) {
    int lookIndex = 0;
    Tree candidateNode = null;
    // looking for begin statement for function
    do {
      ++lookIndex;
      candidateNode = node.getParent().getChild(node.getChildIndex() + lookIndex);

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
        idents = argsNode.getChild(i + 1);
        if (idents == null || idents.getType() != DelphiLexer.TkVariableIdents) {
          return args;
        }
        continue;
      }

      for (int c = 0; c < idents.getChildCount(); ++c) {
        args.put(idents.getChild(c).getText().toLowerCase(), 0);
      }
    }
    return args;
  }

  @Override
  protected void init() {
    super.init();
    List<String> stringProperties = getProperty(EXCLUDED_ARGS);
    for (String prop : stringProperties) {
      excludedArgs.add(prop.toLowerCase());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) {
      return false;
    }
    UnusedArgumentsRule that = (UnusedArgumentsRule) o;
    return excludedArgs.equals(that.excludedArgs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), excludedArgs);
  }
}
