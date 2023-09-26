/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.checks;

import static au.com.integradev.delphi.utils.VariableUtils.isGeneratedFormVariable;

import au.com.integradev.delphi.utils.InterfaceUtils;
import au.com.integradev.delphi.utils.NameConventionUtils;
import com.google.common.base.Splitter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.ForLoopVarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterListNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.api.ast.MethodDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.VarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.VarStatementNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.UnitScope;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "VariableNameRule", repositoryKey = "delph")
@Rule(key = "VariableName")
public class VariableNameCheck extends DelphiCheck {
  private static final String MESSAGE =
      "Rename this variable to match the expected naming convention.";

  @RuleProperty(
      key = "globalPrefixes",
      description =
          "Comma-delimited list of prefixes, global variables must begin with one of these.")
  public String globalPrefixes = "";

  private List<String> globalPrefixesList;

  @Override
  public void start(DelphiCheckContext context) {
    globalPrefixesList = Splitter.on(',').trimResults().splitToList(globalPrefixes);
  }

  @Override
  public DelphiCheckContext visit(VarDeclarationNode varDeclaration, DelphiCheckContext context) {
    if (isGeneratedFormVariable(varDeclaration)) {
      return context;
    }

    boolean globalVariable = varDeclaration.getScope() instanceof UnitScope;
    varDeclaration.getNameDeclarationList().getDeclarations().stream()
        .filter(node -> isViolation(node, globalVariable))
        .forEach(node -> reportIssue(context, node, MESSAGE));

    return context;
  }

  @Override
  public DelphiCheckContext visit(VarStatementNode varStatement, DelphiCheckContext context) {
    varStatement.getNameDeclarationList().getDeclarations().stream()
        .filter(node -> isViolation(node, false))
        .forEach(node -> reportIssue(context, node, MESSAGE));
    return context;
  }

  @Override
  public DelphiCheckContext visit(ForLoopVarDeclarationNode loopVar, DelphiCheckContext context) {
    NameDeclarationNode name = loopVar.getNameDeclarationNode();
    if (isViolation(name, false)) {
      reportIssue(context, name, MESSAGE);
    }
    return context;
  }

  @Override
  public DelphiCheckContext visit(
      FormalParameterListNode parameterListNode, DelphiCheckContext context) {
    var parameterNodes =
        parameterListNode.getParameters().stream()
            .map(FormalParameterData::getNode)
            .collect(Collectors.toUnmodifiableList());

    var methodDeclarationNode = parameterListNode.getFirstParentOfType(MethodDeclarationNode.class);
    var methodDeclaration =
        methodDeclarationNode == null ? null : methodDeclarationNode.getMethodNameDeclaration();

    getParametersToCheck(methodDeclaration, parameterNodes).stream()
        .filter(parameter -> isViolation(parameter, false))
        .forEach(parameter -> reportIssue(context, parameter, MESSAGE));

    return context;
  }

  private static List<NameDeclarationNode> getParametersToCheck(
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
      return !NameConventionUtils.compliesWithPrefix(image, globalPrefixesList);
    }
    return !NameConventionUtils.compliesWithPascalCase(image);
  }
}
