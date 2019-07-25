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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

/** Rule violation for unused function/procedure/method arguments */
public class UnusedArgumentsRule extends DelphiRule {
  private String currentTypeName;
  private Set<String> excludedMethods;
  private List<PossibleUnusedArgument> possibleUnusedArguments;

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    handleTypes(node);
    handleAssignments(node);
    handlePointerOperators(node);
    handleMethods(node, ctx);
    handleEndOfFile(node);
  }

  private void handleTypes(DelphiPMDNode node) {
    if (node.getType() == DelphiLexer.TkNewType) {
      extractTypeName(node);

      Tree typeDecl = node.getFirstChildWithType(DelphiLexer.TkNewTypeDecl);
      handleTypeDecl(typeDecl.getChild(0));
    }
  }

  private void extractTypeName(DelphiPMDNode typeNode) {
    DelphiPMDNode node = typeNode;
    StringBuilder typeName = new StringBuilder();

    do {
      Tree newTypeName = node.getFirstChildWithType(DelphiLexer.TkNewTypeName);

      if (typeName.length() != 0) {
        typeName.insert(0, ".");
      }
      typeName.insert(0, newTypeName.getChild(0).getText().toLowerCase());
      node = findParentType(node);
    } while (node != null);

    currentTypeName = typeName.toString();
  }

  private DelphiPMDNode findParentType(DelphiPMDNode currentNode) {
    Tree node = currentNode;

    while ((node = node.getParent()) != null) {
      if (node.getType() == DelphiLexer.TkNewType) {
        return (DelphiPMDNode) node;
      }
    }

    return null;
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

  /**
   * @param methodNode The method AST node
   * @return whether the method has a directive which will exclude it from this rule
   */
  private boolean hasExcludedDirective(Tree methodNode) {
    for (int i = 0; i < methodNode.getChildCount(); ++i) {
      int type = methodNode.getChild(i).getType();

      if (type == DelphiLexer.OVERRIDE
          || type == DelphiLexer.VIRTUAL
          || type == DelphiLexer.MESSAGE) {
        return true;
      }
    }

    return false;
  }

  /**
   * Excludes methods from this rule if they have been assigned to a variable This indicates that
   * the method has to satisfy some callback method signature
   *
   * @param node The current node
   */
  private void handleAssignments(DelphiPMDNode node) {
    if (node.getType() != DelphiLexer.ASSIGN) {
      return;
    }

    DelphiPMDNode nameNode = node.nextNode();
    if (nameNode == null || nameNode.getType() != DelphiLexer.TkIdentifier) {
      return;
    }

    DelphiPMDNode nextNode = nameNode.nextNode();
    if (nextNode != null && nextNode.getType() == DelphiLexer.DOT) {
      return;
    }

    String methodName = nameNode.getText().toLowerCase();
    excludedMethods.add(currentTypeName + "." + methodName);
  }

  /**
   * Excludes methods from this rule if their pointer address has been passed around This indicates
   * that the method has to satisfy some callback method signature
   *
   * @param node The current node
   */
  private void handlePointerOperators(DelphiPMDNode node) {
    if (node.getType() != DelphiLexer.AT2) {
      return;
    }

    DelphiPMDNode nameNode = node.nextNode();
    if (nameNode == null || nameNode.getType() != DelphiLexer.TkIdentifier) {
      return;
    }

    DelphiPMDNode nextNode = nameNode.nextNode();
    if (nextNode != null && nextNode.getType() == DelphiLexer.DOT) {
      return;
    }

    String methodName = nameNode.getText().toLowerCase();
    excludedMethods.add(currentTypeName + "." + methodName);
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
      // If we already know the method is excluded, we might as well skip all this work.
      return;
    }

    if (notSubProcedureNode(node)) {
      currentTypeName = extractTypeFromMethodName(methodName);
    }

    Map<String, Integer> args = processFunctionArgs(argsNode);
    if (args.isEmpty()) {
      // no arguments
      return;
    }

    List<Tree> beginNodes = findBeginNodes(node);
    if (beginNodes.isEmpty()) {
      return;
    }

    for (Tree beginNode : beginNodes) {
      processFunctionBegin(beginNode, args);
    }

    addViolationsForUnusedArguments(args, ctx, node, methodName);
  }

  private List<Tree> findBeginNodes(DelphiPMDNode node) {
    List<Tree> beginNodes = new ArrayList<>();

    DelphiPMDNode blockDeclSection = node.nextNode();
    if (blockDeclSection == null || blockDeclSection.getType() != DelphiLexer.TkBlockDeclSection) {
      return beginNodes;
    }

    DelphiPMDNode mainBegin = blockDeclSection.nextNode();
    if (mainBegin != null) {
      beginNodes.add(mainBegin);
    }

    findSubProcedureBeginNodes(blockDeclSection, beginNodes);

    return beginNodes;
  }

  private void findSubProcedureBeginNodes(DelphiPMDNode node, List<Tree> beginNodes) {
    final int[] types = {DelphiLexer.PROCEDURE, DelphiLexer.FUNCTION};

    List<DelphiPMDNode> methodNodes =
        node.findAllChildren(types).stream()
            .map(treeNode -> (DelphiPMDNode) treeNode)
            .collect(Collectors.toList());

    for (DelphiPMDNode methodNode : methodNodes) {
      int childIndex = methodNode.getChildIndex();
      Tree parent = methodNode.getParent();

      if (childIndex < parent.getChildCount() - 2) {
        Tree beginNode = parent.getChild(childIndex + 2);
        if (isBlockNode(beginNode)) {
          beginNodes.add(beginNode);
        }
      }
    }
  }

  private boolean notSubProcedureNode(Tree node) {
    return node.getParent().getType() != DelphiLexer.TkBlockDeclSection;
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

  private String extractTypeFromMethodName(String methodName) {
    int dotIndex = methodName.lastIndexOf('.');
    String typeName = "";

    if (dotIndex > 0) {
      typeName = methodName.substring(0, dotIndex);
    }

    return typeName.toLowerCase();
  }

  private boolean isMethodNode(Tree candidateNode) {
    return candidateNode.getType() == DelphiLexer.PROCEDURE
        || candidateNode.getType() == DelphiLexer.FUNCTION;
  }

  /**
   * Marks arguments with 0 usages as possible violations
   *
   * @param args Arguments usage map
   * @param node PMDNode
   * @param methodName Method name
   */
  private void addViolationsForUnusedArguments(
      Map<String, Integer> args, RuleContext ctx, DelphiPMDNode node, String methodName) {
    for (Map.Entry<String, Integer> entry : args.entrySet()) {
      if (entry.getValue() > 0) {
        continue;
      }

      var unusedArg = new PossibleUnusedArgument(this, ctx, node, entry.getKey(), methodName);
      possibleUnusedArguments.add(unusedArg);
    }
  }

  boolean isExcluded(String methodName) {
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

      if (isBlockNode(child)) {
        processFunctionBegin(child, args);
      }
    }
  }

  private boolean isBlockNode(Tree node) {
    return node.getType() == DelphiLexer.BEGIN
        || node.getType() == DelphiLexer.ASM
        || node.getType() == DelphiLexer.TkAssemblerInstructions;
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

  private void handleEndOfFile(DelphiPMDNode node) {
    if (node.getType() != DelphiLexer.DOT) {
      return;
    }

    DelphiPMDNode endNode = node.prevNode();
    if (endNode == null || endNode.getType() != DelphiLexer.END) {
      return;
    }

    for (PossibleUnusedArgument arg : possibleUnusedArguments) {
      arg.processViolation();
    }
  }

  @Override
  protected void init() {
    currentTypeName = "";
    excludedMethods = new HashSet<>();
    possibleUnusedArguments = new ArrayList<>();
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

/**
 * Stores information about a potential unused argument violation. When the end of the file is
 * reached, violations are created from these objects.
 */
class PossibleUnusedArgument {
  private UnusedArgumentsRule rule;
  private RuleContext ctx;
  private DelphiPMDNode node;
  private String argName;
  private String methodName;

  PossibleUnusedArgument(
      UnusedArgumentsRule rule,
      RuleContext ctx,
      DelphiPMDNode node,
      String argName,
      String methodName) {
    this.rule = rule;
    this.ctx = ctx;
    this.node = node;
    this.argName = argName;
    this.methodName = methodName;
  }

  /** Creates a violation for this unused argument (unless the method is excluded) */
  void processViolation() {
    if (rule.isExcluded(methodName)) {
      return;
    }

    rule.addViolation(ctx, node, "Unused argument: '" + argName + "' at " + methodName);
  }
}
