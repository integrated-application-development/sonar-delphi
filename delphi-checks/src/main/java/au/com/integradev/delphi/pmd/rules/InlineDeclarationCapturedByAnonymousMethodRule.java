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

import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.ast.AnonymousMethodNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;

public class InlineDeclarationCapturedByAnonymousMethodRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(NameReferenceNode node, RuleContext data) {
    if (isViolation(node)) {
      addViolation(data, node);
    }
    return super.visit(node, data);
  }

  private static boolean isViolation(NameReferenceNode node) {
    NameDeclaration declaration = node.getNameDeclaration();
    if (declaration instanceof VariableNameDeclaration) {
      VariableNameDeclaration varDeclaration = (VariableNameDeclaration) declaration;
      if (!varDeclaration.isInline()) {
        return false;
      }

      AnonymousMethodNode anonymousMethod = node.getFirstParentOfType(AnonymousMethodNode.class);
      if (anonymousMethod == null) {
        return false;
      }

      DelphiScope declarationScope = varDeclaration.getScope();
      DelphiScope scope = anonymousMethod.getScope();
      while ((scope = scope.getParent()) != null) {
        if (scope == declarationScope) {
          return true;
        }
      }
    }
    return false;
  }
}
