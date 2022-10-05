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
package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyNode;
import org.sonar.plugins.delphi.symbol.declaration.PropertyNameDeclaration;

public class UnusedPropertiesRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(PropertyNode property, RuleContext data) {
    NameDeclarationNode name = property.getPropertyName();
    PropertyNameDeclaration declaration = (PropertyNameDeclaration) name.getNameDeclaration();
    if (isUnused(declaration)) {
      addViolation(data, name);
    }
    return data;
  }

  private static boolean isUnused(PropertyNameDeclaration declaration) {
    return !declaration.isPublished()
        && declaration.getScope().getOccurrencesFor(declaration).isEmpty()
        && declaration.getRedeclarations().stream().allMatch(UnusedPropertiesRule::isUnused);
  }
}
