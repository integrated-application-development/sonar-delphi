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
package au.com.integradev.delphi.pmd.rules;

import static au.com.integradev.delphi.utils.MethodUtils.isMethodStubWithStackUnwinding;

import au.com.integradev.delphi.antlr.ast.node.DelphiAst;
import au.com.integradev.delphi.antlr.ast.node.DelphiAst;
import au.com.integradev.delphi.antlr.ast.node.MethodDeclarationNode;
import au.com.integradev.delphi.antlr.ast.node.MethodImplementationNode;
import au.com.integradev.delphi.antlr.ast.node.MethodNode;
import au.com.integradev.delphi.symbol.NameOccurrence;
import au.com.integradev.delphi.symbol.declaration.MethodDirective;
import au.com.integradev.delphi.symbol.declaration.MethodNameDeclaration;
import au.com.integradev.delphi.symbol.scope.DelphiScope;
import au.com.integradev.delphi.symbol.scope.MethodScope;
import au.com.integradev.delphi.symbol.scope.UnitScope;
import au.com.integradev.delphi.utils.InterfaceUtils;
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.pmd.RuleContext;

public class UnusedMethodsRule extends AbstractDelphiRule {
  private final Set<MethodNameDeclaration> seenMethods = new HashSet<>();

  @Override
  public RuleContext visit(DelphiAst ast, RuleContext data) {
    seenMethods.clear();
    return super.visit(ast, data);
  }

  @Override
  public RuleContext visit(MethodNode method, RuleContext data) {
    MethodNameDeclaration methodDeclaration = method.getMethodNameDeclaration();

    if (!seenMethods.contains(methodDeclaration) && isViolation(method)) {
      addViolation(data, method.getMethodNameNode());
    }

    seenMethods.add(methodDeclaration);

    return super.visit(method, data);
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
