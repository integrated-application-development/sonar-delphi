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

import static au.com.integradev.delphi.utils.MethodUtils.isMethodStubWithStackUnwinding;

import au.com.integradev.delphi.utils.InterfaceUtils;
import java.util.HashSet;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.MethodDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.SonarLintUnsupported;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.MethodScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.UnitScope;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@SonarLintUnsupported
@DeprecatedRuleKey(ruleKey = "UnusedMethodsRule", repositoryKey = "delph")
@Rule(key = "UnusedMethod")
public class UnusedMethodCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this unused method.";

  private final Set<MethodNameDeclaration> seenMethods = new HashSet<>();

  @Override
  public DelphiCheckContext visit(DelphiAst ast, DelphiCheckContext context) {
    seenMethods.clear();
    return super.visit(ast, context);
  }

  @Override
  public DelphiCheckContext visit(MethodNode method, DelphiCheckContext context) {
    MethodNameDeclaration methodDeclaration = method.getMethodNameDeclaration();

    if (!seenMethods.contains(methodDeclaration) && isViolation(method)) {
      reportIssue(context, method.getMethodNameNode(), MESSAGE);
    }

    seenMethods.add(methodDeclaration);

    return super.visit(method, context);
  }

  private static boolean isViolation(MethodNode method) {
    if (method.isPublished()) {
      return false;
    }

    MethodNameDeclaration methodDeclaration = method.getMethodNameDeclaration();
    if (methodDeclaration == null) {
      return false;
    }

    if (methodDeclaration.hasDirective(MethodDirective.OVERRIDE)) {
      return false;
    }

    if (methodDeclaration.hasDirective(MethodDirective.MESSAGE)) {
      return false;
    }

    if (!methodDeclaration.isCallable()) {
      return false;
    }

    if (methodDeclaration.getName().equalsIgnoreCase("Register")
        && methodDeclaration.getScope() instanceof UnitScope) {
      return false;
    }

    if (InterfaceUtils.implementsMethodOnInterface(methodDeclaration)) {
      return false;
    }

    if (isForbiddenConstructor(method)) {
      return false;
    }

    return method.getMethodNameNode().getUsages().stream()
        .allMatch(occurrence -> isWithinMethod(occurrence, methodDeclaration));
  }

  private static boolean isForbiddenConstructor(MethodNode method) {
    if (method instanceof MethodDeclarationNode && method.isConstructor()) {
      DelphiAst ast = method.getAst();
      return ast.findDescendantsOfType(MethodImplementationNode.class).stream()
          .anyMatch(
              implementation -> {
                MethodNameDeclaration declaration = implementation.getMethodNameDeclaration();
                return declaration != null
                    && declaration.equals(method.getMethodNameDeclaration())
                    && isMethodStubWithStackUnwinding(implementation);
              });
    }
    return false;
  }

  private static boolean isWithinMethod(NameOccurrence occurrence, MethodNameDeclaration method) {
    DelphiScope scope = occurrence.getLocation().getScope();
    while (scope != null) {
      if (scope instanceof MethodScope) {
        MethodScope methodScope = (MethodScope) scope;
        if (method.equals(methodScope.getMethodNameDeclaration())) {
          return true;
        }
      }
      scope = scope.getParent();
    }
    return false;
  }
}
