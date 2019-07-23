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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Rule violation for unused function/procedure/method arguments
 */
public class UnusedArgumentsRule extends DelphiRule {
  private String currentTypeName;
  private Set<String> excludedMethods;

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    handleTypes(node);
    handleMethods(node, ctx);
  }

  private void handleTypes(DelphiPMDNode node) {
    if (node.getType() == DelphiLexer.TkNewType) {
      Tree typeName = node.getFirstChildWithType(DelphiLexer.TkNewTypeName);
      handleTypeName(typeName);

      Tree typeDecl = node.getFirstChildWithType(DelphiLexer.TkNewTypeDecl);
      handleTypeDecl(typeDecl.getChild(0));
    }
  }

  private void handleTypeName(Tree typeName) {
    currentTypeName = typeName.getChild(0).getText().toLowerCase();
  }

  private void handleTypeDecl(Tree typeDecl) {
    if (typeDecl == null) {
      return;
    }

    int visibility = DelphiLexer.PUBLISHED;
    for (int i = 0; i < typeDecl.getChildCount(); ++i) {
      Tree child = typeDecl.getChild(i);

      switch (child.getType()) {
        case DelphiLexer.PRIVATE:
        case DelphiLexer.PROTECTED:
        case DelphiLexer.PUBLIC:
        case DelphiLexer.PUBLISHED:
          visibility = child.getType();
          break;

        case DelphiLexer.FUNCTION:
        case DelphiLexer.PROCEDURE:
          handleMethodDeclaration(child, visibility);
          break;

        default:
          // Do nothing
      }
    }
  }

  private void handleMethodDeclaration(Tree child, int visibility) {
    if (visibility == DelphiLexer.PUBLISHED || hasExcludedDirective(child)) {
      String methodName = child.getChild(0).getChild(0).getText().toLowerCase();
      excludedMethods.add(currentTypeName + "." + methodName);
    }
  }

  private boolean hasExcludedDirective(Tree methodNode) {
    for (int i = 0; i < methodNode.getChildCount(); ++i) {
      int type = methodNode.getChild(i).getType();

      if (type == DelphiLexer.OVERRIDE || type == DelphiLexer.VIRTUAL) {
        return true;
      }
    }

    return false;
  }

  private void handleMethods(DelphiPMDNode node, RuleContext ctx) {
    if (!isMethodNode(node) || !isImplementationSection()) {
      return;
    }

    Tree argsNode = node.getFirstChildWithType(DelphiLexer.TkFunctionArgs);
    if (argsNode == null) {
      return;
    }

    String methodName = extractMethodName(node);
    if (isExcluded(methodName)) {
      return;
    }

    Map<String, Integer> args = processFunctionArgs(argsNode);
    if (args.isEmpty()) {
      // no arguments
      return;
    }

    for (Tree beginNode : findBeginNodes(node)) {
      processFunctionBegin(beginNode, args);
    }

    addViolationsForUnusedArguments(args, ctx, node, methodName);
  }

  private List<Tree> findBeginNodes(DelphiPMDNode node) {
    List<Tree> beginNodes = new ArrayList<>();
    DelphiPMDNode blockDeclSection = node.nextNode();

    if (blockDeclSection == null) {
      return beginNodes;
    }

    beginNodes.add(blockDeclSection.nextNode());

    findSubProcedureBeginNodes(blockDeclSection, beginNodes);

    return beginNodes;
  }

  private void findSubProcedureBeginNodes(DelphiPMDNode node, List<Tree> beginNodes) {
    final int[] types = {DelphiLexer.PROCEDURE, DelphiLexer.FUNCTION};

    List<DelphiPMDNode> methodNodes = node.findAllChildren(types).stream()
        .map(treeNode -> (DelphiPMDNode) treeNode)
        .collect(Collectors.toList());

    for (DelphiPMDNode methodNode : methodNodes) {
      int childIndex = methodNode.getChildIndex();
      Tree parent = methodNode.getParent();

      if (childIndex < parent.getChildCount() - 2) {
        Tree beginNode = parent.getChild(childIndex + 2);
        if (beginNode.getType() == DelphiLexer.BEGIN) {
          beginNodes.add(beginNode);
        }
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

  /**
   * Adds violations for any arguments with 0 usages
   *
   * @param args Arguments usage map
   * @param node PMDNode
   * @param methodName Method name
   */
  private void addViolationsForUnusedArguments(Map<String, Integer> args, RuleContext ctx,
      DelphiPMDNode node, String methodName) {
    for (Map.Entry<String, Integer> entry : args.entrySet()) {
      if (entry.getValue() == 0) {
        addViolation(ctx, node, "Unused argument: '" + entry.getKey() + "' at " + methodName);
      }
    }
  }

  private boolean isExcluded(String methodName) {
    return excludedMethods.contains(methodName.toLowerCase());
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
      String key = child.getText();
      if (args.containsKey(key)) {
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
    Map<String, Integer> args = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

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
        args.put(idents.getChild(c).getText(), 0);
      }
    }
    return args;
  }

  @Override
  protected void init() {
    currentTypeName = "";
    excludedMethods = new HashSet<>();
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
