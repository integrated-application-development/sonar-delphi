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
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameter;
import org.sonar.plugins.delphi.antlr.ast.node.IdentifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodBodyNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;

/** Rule violation for unused function/procedure/method arguments */
public class UnusedArgumentsRule extends AbstractDelphiRule {
  private String currentTypeName;
  private Set<String> excludedMethods;
  private List<PossibleUnusedArgument> possibleUnusedArguments;

  @Override
  public RuleContext visit(DelphiAST node, RuleContext data) {
    super.visit(node, data);

    for (PossibleUnusedArgument arg : possibleUnusedArguments) {
      arg.processViolation(this, data);
    }

    return data;
  }

  @Override
  public RuleContext visit(TypeDeclarationNode type, RuleContext data) {
    currentTypeName = type.fullyQualifiedName().toLowerCase();
    type.findDescendantsOfType(MethodDeclarationNode.class).forEach(this::handleMethodDeclaration);

    return super.visit(type, data);
  }

  private void handleMethodDeclaration(MethodDeclarationNode method) {
    if (method.isPublished() || method.isOverride() || method.isVirtual() || method.isMessage()) {
      excludedMethods.add(currentTypeName + "." + method.simpleName().toLowerCase());
    }
  }

  /**
   * Exclude methods from this rule if they have been assigned to a variable. This indicates that
   * the method may have to satisfy some callback method signature
   *
   * @param node AssignmentStatementNode which could have a method assigned to it
   * @param data Rule context
   */
  @Override
  public RuleContext visit(AssignmentStatementNode node, RuleContext data) {
    ExpressionNode assignedValue = node.getValue();
    DelphiNode nameNode = (DelphiNode) assignedValue.jjtGetChild(0);

    if (nameNode instanceof NameReferenceNode
        && ((NameReferenceNode) nameNode).nextName() == null) {
      excludedMethods.add(currentTypeName + "." + nameNode.getImage().toLowerCase());
    }
    return super.visit(node, data);
  }

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    currentTypeName = method.getTypeName().toLowerCase();

    List<FormalParameter> parameters = method.getParameters();
    if (parameters.isEmpty()) {
      return super.visit(method, data);
    }

    String methodName = method.fullyQualifiedName().toLowerCase();
    if (isExcluded(methodName)) {
      // If we already know the method is excluded, we might as well skip all this work.
      return super.visit(method, data);
    }

    // This will pick up sub-procedures as well
    List<MethodBodyNode> bodyNodes = method.findDescendantsOfType(MethodBodyNode.class);
    Map<String, Integer> args = makeArgumentUsageMap(parameters);

    if (!bodyNodes.isEmpty()) {
      for (MethodBodyNode body : bodyNodes) {
        processMethodBody(body, args);
      }

      addViolationsForUnusedArguments(args, method);
    }
    return super.visit(method, data);
  }

  private void processMethodBody(MethodBodyNode body, Map<String, Integer> args) {
    body.getBlock().findDescendantsOfType(IdentifierNode.class).stream()
        .filter(node -> args.containsKey(node.getImage()))
        .forEach(
            node -> {
              String key = node.getImage();
              Integer newValue = args.get(key) + 1;
              args.put(key, newValue);
            });
  }

  private void addViolationsForUnusedArguments(
      Map<String, Integer> args, MethodImplementationNode method) {
    for (Map.Entry<String, Integer> entry : args.entrySet()) {
      if (entry.getValue() > 0) {
        continue;
      }

      var unusedArg = new PossibleUnusedArgument(method, method.getParameter(entry.getKey()));
      possibleUnusedArguments.add(unusedArg);
    }
  }

  boolean isExcluded(String methodName) {
    return excludedMethods.contains(methodName.toLowerCase());
  }

  private static Map<String, Integer> makeArgumentUsageMap(List<FormalParameter> parameters) {
    Map<String, Integer> args = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    for (FormalParameter parameter : parameters) {
      args.put(parameter.getImage(), 0);
    }

    return args;
  }

  @Override
  public void start(RuleContext ctx) {
    currentTypeName = "";
    excludedMethods = new HashSet<>();
    possibleUnusedArguments = new ArrayList<>();
  }
}

/**
 * Stores information about a potential unused argument violation. When the end of the file is
 * reached, violations are created from these objects.
 */
class PossibleUnusedArgument {
  private static final String MESSAGE = "Unused argument: '%s' at %s";

  private final MethodImplementationNode method;
  private final FormalParameter argument;

  PossibleUnusedArgument(MethodImplementationNode method, FormalParameter argument) {
    this.method = method;
    this.argument = argument;
  }

  /** Creates a violation for this unused argument (unless the method is excluded) */
  void processViolation(UnusedArgumentsRule rule, Object data) {
    if (rule.isExcluded(method.fullyQualifiedName().toLowerCase())) {
      return;
    }

    String message = String.format(MESSAGE, argument.getImage(), method.fullyQualifiedName());
    rule.addViolationWithMessage(data, argument.getNode(), message);
  }
}
