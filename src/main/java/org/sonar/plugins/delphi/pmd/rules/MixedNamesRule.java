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
import java.util.stream.Collectors;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.IdentifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodBodyNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodHeadingNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarDeclarationNode;

@Smell(
    minutes = 60,
    reason =
        "Won't handle function name mixing when the type is declared in the implementation. "
            + "Also doesn't handle argument names. "
            + "Pretty much all of this should be replaced with a symbol table.",
    type = SmellType.WRONG_LOGIC)
public class MixedNamesRule extends AbstractDelphiRule {

  private static final String MIXED_VARIABLE_NAME =
      "Avoid mixing variable names (found: '%s' expected: '%s').";
  private static final String MIXED_METHOD_NAME =
      "Avoid mixing method names (found: '%s' expected: '%s').";

  private final List<String> functionNames = new ArrayList<>();
  private final List<String> variableNames = new ArrayList<>();

  @Override
  public void start(RuleContext ctx) {
    functionNames.clear();
    variableNames.clear();
  }

  @Override
  public RuleContext visit(MethodHeadingNode methodHeading, RuleContext data) {
    String name = methodHeading.getQualifiedName();

    if (methodHeading.isInterfaceSection()) {
      functionNames.add(name);
    } else {
      String globalName = getGlobalName(name, functionNames);
      if (!name.equals(globalName)) {
        String message = String.format(MIXED_METHOD_NAME, name, globalName);
        addViolationWithMessage(data, methodHeading.getMethodName(), message);
      }
    }

    return super.visit(methodHeading, data);
  }

  private void handleVarSection(VarDeclarationNode varDecl) {
    variableNames.addAll(
        varDecl.getIdentifierList().getIdentifiers().stream()
            .map(IdentifierNode::getImage)
            .collect(Collectors.toList()));
  }

  @Override
  public RuleContext visit(MethodBodyNode methodBody, RuleContext data) {
    checkVariableNames(methodBody, data);
    return super.visit(methodBody, data);
  }

  private void checkVariableNames(MethodBodyNode methodBody, RuleContext data) {
    if (!methodBody.hasDeclarationSection()) {
      return;
    }

    methodBody
        .getDeclarationSection()
        .findDescendantsOfType(VarDeclarationNode.class)
        .forEach(this::handleVarSection);

    methodBody.getBlock().findDescendantsOfType(IdentifierNode.class).stream()
        .filter(this::isUnqualifiedIdentifier)
        .forEach(identifier -> checkVariableName(identifier, data));

    variableNames.clear();
  }

  private void checkVariableName(IdentifierNode identifier, Object data) {
    String name = identifier.getImage();
    String globalName = getGlobalName(name, variableNames);
    if (!globalName.equals(name)) {
      String message = String.format(MIXED_VARIABLE_NAME, identifier.getImage(), globalName);
      addViolationWithMessage(data, identifier, message);
    }
  }

  private boolean isUnqualifiedIdentifier(DelphiNode node) {
    Node prevNode = node.prevNode();
    return prevNode == null || prevNode.jjtGetId() != DelphiLexer.DOT;
  }

  private String getGlobalName(String name, List<String> globalNames) {
    for (String globalName : globalNames) {
      if (name.equalsIgnoreCase(globalName)) {
        return globalName;
      }
    }
    return name;
  }
}
