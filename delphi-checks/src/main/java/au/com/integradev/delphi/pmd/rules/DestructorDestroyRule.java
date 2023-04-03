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

import org.sonar.plugins.communitydelphi.api.ast.MethodDeclarationNode;
import net.sourceforge.pmd.RuleContext;

public class DestructorDestroyRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(MethodDeclarationNode method, RuleContext data) {
    if (isViolation(method)) {
      addViolation(data, method.getMethodNameNode());
    }
    return super.visit(method, data);
  }

  private boolean isViolation(MethodDeclarationNode method) {
    if (!method.isDestructor() || method.isClassMethod()) {
      return false;
    }

    return !(method.simpleName().equalsIgnoreCase("Destroy")
        && method.getParameters().isEmpty()
        && method.isOverride());
  }
}