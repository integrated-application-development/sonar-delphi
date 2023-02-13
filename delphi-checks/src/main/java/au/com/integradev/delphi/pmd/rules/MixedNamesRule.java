/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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

import au.com.integradev.delphi.antlr.ast.node.UnitImportNode;
import au.com.integradev.delphi.symbol.NameDeclaration;
import au.com.integradev.delphi.symbol.NameOccurrence;
import au.com.integradev.delphi.symbol.declaration.UnitImportNameDeclaration;
import au.com.integradev.delphi.symbol.declaration.UnitNameDeclaration;
import net.sourceforge.pmd.RuleContext;
import au.com.integradev.delphi.antlr.ast.node.Node;
import org.apache.commons.lang3.StringUtils;

public class MixedNamesRule extends AbstractDelphiRule {
  private void checkNameOccurrenceForViolations(
      Node node, RuleContext data, NameOccurrence occurrence) {
    NameDeclaration declaration = occurrence.getNameDeclaration();

    if (declaration != null && !occurrence.isSelf()) {
      String actual = occurrence.getImage();
      String expected = declaration.getImage();

      if (!expected.equals(actual)) {
        addViolationWithMessage(
            data,
            node,
            "Avoid mixing names (found: ''{0}'' expected: ''{1}'').",
            new Object[] {actual, expected});
      }
    }
  }

  private void checkUnitReferenceForViolations(
      Node node, RuleContext data, String importName, UnitImportNameDeclaration importDeclaration) {
    UnitNameDeclaration originalDeclaration = importDeclaration.getOriginalDeclaration();

    if (originalDeclaration != null) {
      String unitName = originalDeclaration.fullyQualifiedName();

      // Only add violations on import names that are not aliases and do not match the original case
      if (StringUtils.endsWithIgnoreCase(unitName, importName) && !unitName.endsWith(importName)) {
        String matchingSegment = unitName.substring(unitName.length() - importName.length());

        addViolationWithMessage(
            data,
            node,
            "Avoid mixing names (found: ''{0}'' expected: ''{1}'').",
            new Object[] {importName, matchingSegment});
      }
    }
  }

  @Override
  public RuleContext visit(NameReferenceNode reference, RuleContext data) {
    NameDeclaration declaration = reference.getNameDeclaration();
    NameOccurrence occurrence = reference.getNameOccurrence();

    if (occurrence != null) {
      if (declaration instanceof UnitImportNameDeclaration) {
        // Checks the occurrence against the original unit declaration instead of the import
        // declaration
        checkUnitReferenceForViolations(
            reference.getIdentifier(),
            data,
            occurrence.getImage(),
            (UnitImportNameDeclaration) declaration);
      } else {
        checkNameOccurrenceForViolations(reference.getIdentifier(), data, occurrence);
      }
    }

    return super.visit(reference, data);
  }

  @Override
  public RuleContext visit(UnitImportNode importNode, RuleContext data) {
    UnitImportNameDeclaration declaration = importNode.getImportNameDeclaration();
    checkUnitReferenceForViolations(
        importNode.getNameNode(), data, declaration.fullyQualifiedName(), declaration);
    return super.visit(importNode, data);
  }
}
