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
package org.sonar.plugins.communitydelphi.pmd.rules;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import org.sonar.plugins.communitydelphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.ExpressionStatementNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.MethodNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.StatementNode;
import org.sonar.plugins.communitydelphi.symbol.declaration.MethodDirective;
import org.sonar.plugins.communitydelphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.type.Type;
import org.sonar.plugins.communitydelphi.type.Type.ScopedType;

public class InheritedMethodWithNoCodeRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    DelphiNode violationNode = findViolation(method);
    if (violationNode != null) {
      addViolation(data, violationNode);
    }

    return super.visit(method, data);
  }

  private static DelphiNode findViolation(MethodImplementationNode method) {
    CompoundStatementNode block = method.getStatementBlock();
    if (block == null) {
      return null;
    }

    List<StatementNode> statements = block.getStatements();
    if (statements.size() != 1) {
      return null;
    }

    StatementNode statement = statements.get(0);
    ExpressionNode expr = null;

    if (statement instanceof ExpressionStatementNode) {
      expr = ((ExpressionStatementNode) statement).getExpression();
    } else if (statement instanceof AssignmentStatementNode) {
      AssignmentStatementNode assignment = (AssignmentStatementNode) statement;
      if (assignment.getAssignee().isResult()) {
        expr = assignment.getValue();
      }
    }

    if (isInheritedCall(method, expr)
        && !isVisibilityChanged(method)
        && !isAddingMeaningfulDirectives(method)) {
      return statement;
    }

    return null;
  }

  private static Stream<Type> concreteParentTypesStream(Type type) {
    return type.parents().stream()
        .filter(Type::isClass)
        .findFirst()
        .map(value -> Stream.concat(Stream.of(value), concreteParentTypesStream(value)))
        .orElseGet(Stream::empty);
  }

  private static List<MethodNameDeclaration> getParentMethodDeclarations(
      MethodImplementationNode method) {
    TypeNameDeclaration typeDeclaration = method.getTypeDeclaration();
    MethodNameDeclaration nameDeclaration = method.getMethodNameDeclaration();
    if (typeDeclaration == null || nameDeclaration == null) {
      return Collections.emptyList();
    }

    return concreteParentTypesStream(typeDeclaration.getType())
        .map(ScopedType.class::cast)
        .flatMap(type -> type.typeScope().getMethodDeclarations().stream())
        .filter(methodDeclaration -> isOverriddenMethod(methodDeclaration, nameDeclaration))
        .collect(Collectors.toUnmodifiableList());
  }

  private static boolean isVisibilityChanged(MethodImplementationNode method) {
    List<MethodNameDeclaration> parentMethods = getParentMethodDeclarations(method);
    if (parentMethods.isEmpty() || method.getMethodNameDeclaration() == null) {
      return true;
    }

    MethodNameDeclaration parentMethod = parentMethods.get(0);
    return parentMethod.getVisibility().ordinal()
        != method.getMethodNameDeclaration().getVisibility().ordinal();
  }

  private static boolean isAddingMeaningfulDirectives(MethodImplementationNode method) {
    List<MethodNameDeclaration> parentMethods = getParentMethodDeclarations(method);
    if (parentMethods.isEmpty() || method.getMethodNameDeclaration() == null) {
      return false;
    }

    MethodNameDeclaration parentMethod = parentMethods.get(0);
    Set<MethodDirective> newDirectives =
        method.getMethodNameDeclaration().getDirectives().stream()
            .filter(Predicate.not(parentMethod.getDirectives()::contains))
            .collect(Collectors.toSet());
    return newDirectives.contains(MethodDirective.REINTRODUCE)
        || newDirectives.contains(MethodDirective.VIRTUAL);
  }

  private static boolean isInheritedCall(MethodImplementationNode method, ExpressionNode expr) {
    if (!(expr instanceof PrimaryExpressionNode)) {
      return false;
    }

    PrimaryExpressionNode expression = (PrimaryExpressionNode) expr;
    if (expression.isBareInherited()) {
      return true;
    }

    if (!expression.isInheritedCall()) {
      return false;
    }

    Node reference = expression.jjtGetChild(1);
    String methodName = method.simpleName();
    if (!(reference instanceof NameReferenceNode && reference.hasImageEqualTo(methodName))) {
      return false;
    }

    Node argumentList = expression.jjtGetChild(2);
    if (argumentList != null && !(argumentList instanceof ArgumentListNode)) {
      return false;
    }

    return argumentSignaturesMatch(method, (ArgumentListNode) argumentList);
  }

  private static boolean argumentSignaturesMatch(MethodNode method, ArgumentListNode argumentList) {
    List<FormalParameterData> parameters = method.getParameters();
    List<ExpressionNode> arguments =
        (argumentList == null) ? Collections.emptyList() : argumentList.getArguments();

    if (arguments.size() != parameters.size()) {
      return false;
    }

    for (int i = 0; i < arguments.size(); ++i) {
      if (!arguments.get(i).hasImageEqualTo(parameters.get(i).getImage())) {
        return false;
      }
    }

    return true;
  }

  private static boolean isOverriddenMethod(
      MethodNameDeclaration parent, MethodNameDeclaration child) {
    return parent.getName().equalsIgnoreCase(child.getName())
        && parent.getParameters().equals(child.getParameters());
  }
}
