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
import org.sonar.plugins.delphi.antlr.ast.node.FieldDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarNameDeclarationNode;
import org.sonar.plugins.delphi.utils.NameConventionUtils;

public class FieldNameRule extends AbstractDelphiRule {
  private static final String FIELD_PREFIX = "F";

  @Override
  public RuleContext visit(FieldDeclarationNode field, RuleContext data) {
    if (field.isPrivate() || field.isProtected()) {
      for (VarNameDeclarationNode identifier : field.getIdentifierList().getIdentifiers()) {
        if (!NameConventionUtils.compliesWithPrefix(identifier.getImage(), FIELD_PREFIX)) {
          addViolation(data, identifier);
        }
      }
    }

    return super.visit(field, data);
  }
}
