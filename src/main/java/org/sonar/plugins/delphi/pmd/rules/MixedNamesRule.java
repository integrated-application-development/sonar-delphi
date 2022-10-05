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
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;

public class MixedNamesRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(NameReferenceNode reference, RuleContext data) {
    DelphiNameOccurrence occurrence = reference.getNameOccurrence();
    DelphiNameDeclaration declaration = reference.getNameDeclaration();

    if (occurrence != null && declaration != null && !occurrence.isSelf()) {
      String actual = occurrence.getImage();
      String expected = declaration.getImage();
      if (!expected.equals(actual)) {
        addViolationWithMessage(
            data,
            reference.getIdentifier(),
            "Avoid mixing names (found: ''{0}'' expected: ''{1}'').",
            new Object[] {actual, expected});
      }
    }

    return super.visit(reference, data);
  }
}
