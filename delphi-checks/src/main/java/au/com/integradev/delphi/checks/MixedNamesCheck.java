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
package au.com.integradev.delphi.checks;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.AttributeNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.UnitImportNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "MixedNamesRule", repositoryKey = "delph")
@Rule(key = "MixedNames")
public class MixedNamesCheck extends DelphiCheck {
  private static final String MESSAGE = "Avoid mixing names (found: \"%s\" expected: \"%s\").";
  private static final Pattern ATTRIBUTE_REGEX =
      Pattern.compile("Attribute$", Pattern.CASE_INSENSITIVE);

  private void checkNameAgainstValidOptions(
      DelphiNode node, DelphiCheckContext context, String actualName, Set<String> expectedNames) {
    if (!expectedNames.contains(actualName)) {
      reportIssue(
          context, node, String.format(MESSAGE, actualName, String.join(" or ", expectedNames)));
    }
  }

  private void checkAttributeTypeReferenceForViolations(
      DelphiNode node, DelphiCheckContext context, NameOccurrence occurrence) {
    NameDeclaration declaration = occurrence.getNameDeclaration();

    if (declaration != null && !occurrence.isSelf()) {
      String actual = occurrence.getImage();

      Set<String> expected =
          Set.of(
              declaration.getImage(),
              ATTRIBUTE_REGEX.matcher(declaration.getImage()).replaceFirst(""));

      checkNameAgainstValidOptions(node, context, actual, expected);
    }
  }

  private void checkNameOccurrenceForViolations(
      DelphiNode node, DelphiCheckContext context, NameOccurrence occurrence) {
    NameDeclaration declaration = occurrence.getNameDeclaration();

    if (declaration != null && !occurrence.isSelf()) {
      String actual = occurrence.getImage();
      checkNameAgainstValidOptions(node, context, actual, Set.of(declaration.getImage()));
    }
  }

  private void checkUnitReferenceForViolations(
      DelphiNode node,
      DelphiCheckContext context,
      String importName,
      UnitImportNameDeclaration importDeclaration) {
    UnitNameDeclaration originalDeclaration = importDeclaration.getOriginalDeclaration();

    if (originalDeclaration != null) {
      String unitName = originalDeclaration.fullyQualifiedName();

      // Only add violations on import names that are not aliases and do not match the original case
      if (StringUtils.endsWithIgnoreCase(unitName, importName) && !unitName.endsWith(importName)) {
        String matchingSegment = unitName.substring(unitName.length() - importName.length());
        reportIssue(context, node, String.format(MESSAGE, importName, matchingSegment));
      }
    }
  }

  @Override
  public DelphiCheckContext visit(NameReferenceNode reference, DelphiCheckContext context) {
    NameDeclaration declaration = reference.getNameDeclaration();
    NameOccurrence occurrence = reference.getNameOccurrence();

    if (occurrence != null) {
      if (declaration instanceof UnitImportNameDeclaration) {
        // Checks the occurrence against the original unit declaration instead of the import
        // declaration
        checkUnitReferenceForViolations(
            reference.getIdentifier(),
            context,
            occurrence.getImage(),
            (UnitImportNameDeclaration) declaration);
      } else {
        checkNameOccurrenceForViolations(reference.getIdentifier(), context, occurrence);
      }
    }

    return super.visit(reference, context);
  }

  @Override
  public DelphiCheckContext visit(UnitImportNode importNode, DelphiCheckContext context) {
    UnitImportNameDeclaration declaration = importNode.getImportNameDeclaration();
    checkUnitReferenceForViolations(
        importNode.getNameNode(), context, declaration.fullyQualifiedName(), declaration);
    return super.visit(importNode, context);
  }

  @Override
  public DelphiCheckContext visit(AttributeNode attributeNode, DelphiCheckContext context) {
    List<NameReferenceNode> nameReferences = attributeNode.getNameReference().flatten();

    for (int i = 0; i + 1 < nameReferences.size(); i++) {
      context = super.visit(nameReferences.get(i), context);
    }

    NameReferenceNode lastNameReference = nameReferences.get(nameReferences.size() - 1);
    NameOccurrence occurrence = lastNameReference.getNameOccurrence();

    if (occurrence != null) {
      checkAttributeTypeReferenceForViolations(
          lastNameReference.getIdentifier(), context, occurrence);
    }

    ArgumentListNode argumentListNode = attributeNode.getArgumentList();
    if (argumentListNode != null) {
      return super.visit(argumentListNode, context);
    } else {
      return context;
    }
  }
}
