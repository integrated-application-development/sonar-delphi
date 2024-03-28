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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.ast.utils.ExpressionNodeUtils;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineKind;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ScopedType;

@Rule(key = "RedundantInherited")
public class RedundantInheritedCheck extends DelphiCheck {
  private static final String MESSAGE = "Redundant inherited calls should be removed";

  @Override
  public DelphiCheckContext visit(RoutineImplementationNode routine, DelphiCheckContext context) {
    List<DelphiNode> violationNodes = findViolations(routine);
    for (DelphiNode violationNode : violationNodes) {
      reportIssue(context, violationNode, MESSAGE);
    }

    return super.visit(routine, context);
  }

  private static List<DelphiNode> findViolations(RoutineImplementationNode routine) {
    CompoundStatementNode block = routine.getStatementBlock();
    if (block == null) {
      return Collections.emptyList();
    }

    List<StatementNode> statements = block.getStatements();
    if (statements.isEmpty()) {
      return Collections.emptyList();
    }

    List<RoutineNameDeclaration> inheritedMethods = getParentMethodDeclarations(routine);
    if (!inheritedMethods.isEmpty()) {
      return Collections.emptyList();
    }

    return statements.stream()
        .filter((statement) -> statement instanceof ExpressionStatementNode)
        .map((statement) -> ((ExpressionStatementNode) statement).getExpression())
        .filter(ExpressionNodeUtils::isBareInherited)
        .collect(Collectors.toList());
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
