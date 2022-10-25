/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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

import static org.sonar.plugins.delphi.utils.VariableUtils.isGeneratedFormVariable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.antlr.ast.node.ForLoopVarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterListNode;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.delphi.antlr.ast.node.MethodDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarStatementNode;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.UnitScope;
import org.sonar.plugins.delphi.utils.InterfaceUtils;
import org.sonar.plugins.delphi.utils.NameConventionUtils;

public class VariableNameRule extends AbstractDelphiRule {
  private static final PropertyDescriptor<List<String>> GLOBAL_PREFIXES =
      PropertyFactory.stringListProperty("global_prefixes")
          .desc("If defined, global variables must begin with one of these prefixes.")
          .emptyDefaultValue()
          .build();

  public VariableNameRule() {
    definePropertyDescriptor(GLOBAL_PREFIXES);
  }

  @Override
  public RuleContext visit(VarDeclarationNode varDeclaration, RuleContext data) {
    if (isGeneratedFormVariable(varDeclaration)) {
      return data;
    }

    boolean globalVariable = varDeclaration.getScope() instanceof UnitScope;
    varDeclaration.getNameDeclarationList().getDeclarations().stream()
        .filter(node -> isViolation(node, globalVariable))
        .forEach(node -> addViolation(data, node));

    return data;
  }

  @Override
  public RuleContext visit(VarStatementNode varStatement, RuleContext data) {
    varStatement.getNameDeclarationList().getDeclarations().stream()
        .filter(node -> isViolation(node, false))
        .forEach(node -> addViolation(data, node));
    return data;
  }

  @Override
  public RuleContext visit(ForLoopVarDeclarationNode loopVar, RuleContext data) {
    NameDeclarationNode name = loopVar.getNameDeclarationNode();
    if (isViolation(name, false)) {
      addViolation(data, name);
    }
    return data;
  }

  @Override
  public RuleContext visit(FormalParameterListNode parameterListNode, RuleContext data) {
    var parameterNodes =
        parameterListNode.getParameters().stream()
            .map(FormalParameterData::getNode)
            .collect(Collectors.toUnmodifiableList());

    var methodDeclarationNode = parameterListNode.getFirstParentOfType(MethodDeclarationNode.class);
    var methodDeclaration =
        methodDeclarationNode == null ? null : methodDeclarationNode.getMethodNameDeclaration();

    getParametersToCheck(methodDeclaration, parameterNodes).stream()
        .filter(param -> isViolation(param, false))
        .forEach(param -> addViolation(data, param));

    return data;
  }

  private List<NameDeclarationNode> getParametersToCheck(
      MethodNameDeclaration methodDeclaration, List<NameDeclarationNode> parameterNodes) {
    if (methodDeclaration == null) {
      return parameterNodes;
    }

    List<Set<String>> paramNamesFromInterfaces =
        parameterNodes.stream()
            .map(x -> new HashSet<String>())
            .collect(Collectors.toUnmodifiableList());

    InterfaceUtils.findImplementedInterfaceMethodDeclarations(methodDeclaration).stream()
        .map(MethodNameDeclaration::getParameters)
        .forEach(
            params -> {
              assert (params.size() == paramNamesFromInterfaces.size());
              for (int i = 0; i < paramNamesFromInterfaces.size(); i++) {
                paramNamesFromInterfaces.get(i).add(params.get(i).getImage());
              }
            });

    return IntStream.range(0, parameterNodes.size())
        .filter(
            index -> {
              var actualImage = parameterNodes.get(index).getImage();
              return !paramNamesFromInterfaces.get(index).contains(actualImage);
            })
        .mapToObj(parameterNodes::get)
        .collect(Collectors.toUnmodifiableList());
  }

  private boolean isViolation(NameDeclarationNode name, boolean globalVariable) {
    String image = name.getImage();
    if (globalVariable) {
      return !NameConventionUtils.compliesWithPrefix(image, getProperty(GLOBAL_PREFIXES));
    }
    return !NameConventionUtils.compliesWithPascalCase(image);
  }
}
