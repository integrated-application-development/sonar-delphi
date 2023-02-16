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
package au.com.integradev.delphi.pmd.rules;

import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.ast.MethodDeclarationNode;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ScopedType;
import net.sourceforge.pmd.RuleContext;

public class MethodNameRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(MethodDeclarationNode method, RuleContext data) {
    if (isViolation(method) && !isExcluded(method)) {
      addViolation(data, method);
    }
    return super.visit(method, data);
  }

  private boolean isViolation(MethodDeclarationNode method) {
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
    return type.parents().stream()
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
