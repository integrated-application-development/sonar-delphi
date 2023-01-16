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

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.communitydelphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.symbol.declaration.MethodDirective;
import org.sonar.plugins.communitydelphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.utils.InterfaceUtils;

public class EmptyMethodRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    if (method.isEmptyMethod() && shouldAddViolation(method)) {
      addViolation(data, method.getMethodNameNode());
    }
    return super.visit(method, data);
  }

  private boolean shouldAddViolation(MethodImplementationNode method) {
    DelphiNode block = method.getBlock();

    if (block != null && block.getComments().isEmpty()) {
      // All exclusions aside, an explanatory comment is mandatory
      return true;
    }

    MethodNameDeclaration declaration = method.getMethodNameDeclaration();
    if (declaration == null) {
      return true;
    }

    return !declaration.hasDirective(MethodDirective.OVERRIDE)
        && !declaration.hasDirective(MethodDirective.VIRTUAL)
        && !InterfaceUtils.implementsMethodOnInterface(declaration);
  }
}
