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
package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.symbol.declaration.MethodDirective;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;

public class DestructorWithoutInheritedStatementRule extends NoInheritedStatementRule {

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    if (isDestructorLike(method)) {
      checkViolation(method, data);
    }
    return super.visit(method, data);
  }

  private static boolean isDestructorLike(MethodImplementationNode method) {
    if (method.isDestructor()) {
      return true;
    }

    MethodNameDeclaration declaration = method.getMethodNameDeclaration();
    if (declaration != null) {
      String name = declaration.getName();

      return declaration.hasDirective(MethodDirective.OVERRIDE)
          && (name.equalsIgnoreCase("Deinit") || name.equalsIgnoreCase("Teardown"));
    }

    return false;
  }
}
