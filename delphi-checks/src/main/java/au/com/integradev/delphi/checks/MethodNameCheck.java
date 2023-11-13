/*
 * Sonar Delphi Plugin
 * Copyright (C) 2015 Fabricio Colombo
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

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.MethodDeclarationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ScopedType;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "MethodNameRule", repositoryKey = "delph")
@Rule(key = "MethodName")
public class MethodNameCheck extends DelphiCheck {
  private static final String MESSAGE =
      "Rename this method to match the expected naming convention";

  @Override
  public DelphiCheckContext visit(MethodDeclarationNode method, DelphiCheckContext context) {
    if (isViolation(method) && !isExcluded(method)) {
      reportIssue(context, method, MESSAGE);
    }
    return super.visit(method, context);
  }

  private static boolean isViolation(MethodDeclarationNode method) {
    String name = method.simpleName();
    return Character.isLowerCase(name.charAt(0));
  }

  private static boolean isExcluded(MethodDeclarationNode node) {
    MethodNameDeclaration method = node.getMethodNameDeclaration();
    TypeNameDeclaration typeDeclaration = node.getTypeDeclaration();
    if (method == null || typeDeclaration == null) {
      return false;
    }

    Type type = typeDeclaration.getType();
    if (method.isPublished() && !type.isInterface()) {
      return true;
    } else {
      return hasOverriddenMethodDeclarationInAncestor(type, method);
    }
  }

  private static boolean hasOverriddenMethodDeclarationInAncestor(
      Type type, MethodNameDeclaration method) {
    return type.ancestorList().stream()
        .anyMatch(
            parent ->
                hasOverriddenMethodDeclaration(parent, method)
                    || hasOverriddenMethodDeclarationInAncestor(parent, method));
  }

  private static boolean hasOverriddenMethodDeclaration(Type type, MethodNameDeclaration method) {
    if (!(type instanceof ScopedType)) {
      return false;
    }

    return ((ScopedType) type)
        .typeScope().getMethodDeclarations().stream()
            .anyMatch(overridden -> isOverriddenMethodDeclaration(overridden, method));
  }

  private static boolean isOverriddenMethodDeclaration(
      MethodNameDeclaration overridden, MethodNameDeclaration method) {
    return overridden.getImage().equals(method.getImage())
        && overridden.hasSameParameterTypes(method)
        && overridden.getTypeParameters().equals(method.getTypeParameters())
        && overridden.isClassInvocable() == method.isClassInvocable();
  }
}
