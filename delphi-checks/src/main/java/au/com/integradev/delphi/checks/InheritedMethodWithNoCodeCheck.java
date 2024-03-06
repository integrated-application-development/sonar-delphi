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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.ast.utils.ExpressionNodeUtils;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineKind;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ScopedType;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "InheritedMethodWithNoCodeRule", repositoryKey = "delph")
@Rule(key = "InheritedMethodWithNoCode")
public class InheritedMethodWithNoCodeCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this useless method override.";

  @Override
  public DelphiCheckContext visit(RoutineImplementationNode routine, DelphiCheckContext context) {
    DelphiNode violationNode = findViolation(routine);
    if (violationNode != null) {
      reportIssue(context, violationNode, MESSAGE);
    }

    return super.visit(routine, context);
  }

  private static DelphiNode findViolation(RoutineImplementationNode routine) {
    CompoundStatementNode block = routine.getStatementBlock();
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
      if (ExpressionNodeUtils.isResult(assignment.getAssignee())) {
        expr = assignment.getValue();
      }
    }

    if (isInheritedCall(routine, expr)
        && !isVisibilityChanged(routine)
        && !isAddingMeaningfulDirectives(routine)) {
      return statement;
    }

    return null;
  }

  private static Stream<Type> concreteParentTypesStream(Type type) {
    return type.ancestorList().stream()
        .filter(Type::isClass)
        .findFirst()
        .map(value -> Stream.concat(Stream.of(value), concreteParentTypesStream(value)))
        .orElseGet(Stream::empty);
  }

  private static List<RoutineNameDeclaration> getParentMethodDeclarations(
      RoutineImplementationNode method) {
    TypeNameDeclaration typeDeclaration = method.getTypeDeclaration();
    RoutineNameDeclaration nameDeclaration = method.getRoutineNameDeclaration();
    if (typeDeclaration == null || nameDeclaration == null) {
      return Collections.emptyList();
    }

    return concreteParentTypesStream(typeDeclaration.getType())
        .map(ScopedType.class::cast)
        .flatMap(type -> type.typeScope().getRoutineDeclarations().stream())
        .filter(methodDeclaration -> isOverriddenMethod(methodDeclaration, nameDeclaration))
        .collect(Collectors.toUnmodifiableList());
  }

  private static boolean isVisibilityChanged(RoutineImplementationNode method) {
    List<RoutineNameDeclaration> parentMethods = getParentMethodDeclarations(method);
    if (parentMethods.isEmpty() || method.getRoutineNameDeclaration() == null) {
      return true;
    }

    RoutineNameDeclaration parentMethod = parentMethods.get(0);
    return parentMethod.getVisibility().ordinal()
        != method.getRoutineNameDeclaration().getVisibility().ordinal();
  }

  private static boolean isAddingMeaningfulDirectives(RoutineImplementationNode method) {
    List<RoutineNameDeclaration> parentMethods = getParentMethodDeclarations(method);
    if (parentMethods.isEmpty() || method.getRoutineNameDeclaration() == null) {
      return false;
    }

    RoutineNameDeclaration parentMethod = parentMethods.get(0);
    Set<RoutineDirective> newDirectives =
        method.getRoutineNameDeclaration().getDirectives().stream()
            .filter(Predicate.not(parentMethod.getDirectives()::contains))
            .collect(Collectors.toSet());
    return newDirectives.contains(RoutineDirective.REINTRODUCE)
        || newDirectives.contains(RoutineDirective.VIRTUAL);
  }

  private static boolean isInheritedCall(RoutineImplementationNode method, ExpressionNode expr) {
    if (!(expr instanceof PrimaryExpressionNode)) {
      return false;
    }

    PrimaryExpressionNode expression = (PrimaryExpressionNode) expr;
    if (ExpressionNodeUtils.isBareInherited(expression)) {
      return true;
    }

    if (!ExpressionNodeUtils.isInherited(expression)) {
      return false;
    }

    Node reference = expression.getChild(1);
    String methodName = method.simpleName();
    if (!(reference instanceof NameReferenceNode
        && reference.getImage().equalsIgnoreCase(methodName))) {
      return false;
    }

    Node argumentList = expression.getChild(2);
    if (argumentList != null && !(argumentList instanceof ArgumentListNode)) {
      return false;
    }

    return argumentSignaturesMatch(method, (ArgumentListNode) argumentList);
  }

  private static boolean argumentSignaturesMatch(
      RoutineNode routine, ArgumentListNode argumentList) {
    List<FormalParameterData> parameters = routine.getParameters();
    List<ExpressionNode> arguments =
        (argumentList == null) ? Collections.emptyList() : argumentList.getArguments();

    if (arguments.size() != parameters.size()) {
      return false;
    }

    for (int i = 0; i < arguments.size(); ++i) {
      if (!arguments.get(i).getImage().equalsIgnoreCase(parameters.get(i).getImage())) {
        return false;
      }
    }

    return true;
  }

  private static boolean isOverriddenMethod(
      RoutineNameDeclaration parent, RoutineNameDeclaration child) {
    if (parent.getName().equalsIgnoreCase(child.getName())
        && parent.getParameters().equals(child.getParameters())) {
      if (parent.isClassInvocable()) {
        if (parent.getRoutineKind() == RoutineKind.CONSTRUCTOR
            || parent.getRoutineKind() == RoutineKind.DESTRUCTOR) {
          // An instance constructor or destructor cannot inherit from a class constructor or
          // destructor
          return child.isClassInvocable();
        } else {
          // Any other type of invocable can inherit from a class invocable
          return true;
        }
      } else {
        // A class invocable cannot inherit from an instance invocable
        return !child.isClassInvocable();
      }
    } else {
      return false;
    }
  }
}
