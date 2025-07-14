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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.Strings;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.AttributeNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.UnitImportNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFix;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFixEdit;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "MixedNamesRule", repositoryKey = "delph")
@Rule(key = "MixedNames")
public class MixedNamesCheck extends DelphiCheck {
  private static final String MESSAGE = "Avoid mixing names (found: \"%s\" expected: \"%s\").";
  private static final String QUICK_FIX_MESSAGE = "Correct to \"%s\"";

  private Set<String> excludedSet;

  @RuleProperty(
      key = "excludedNames",
      description = "List of names to ignore, separated by a comma.")
  public String excludedNames = "";

  @Override
  public void start(DelphiCheckContext context) {
    excludedSet =
        ImmutableSortedSet.copyOf(
            String.CASE_INSENSITIVE_ORDER, Splitter.on(',').trimResults().split(excludedNames));
  }

  @Override
  public DelphiCheckContext visit(NameReferenceNode reference, DelphiCheckContext context) {
    NameDeclaration declaration = reference.getNameDeclaration();
    NameOccurrence occurrence = reference.getNameOccurrence();

    if (declaration != null) {
      if (excludedSet.contains(occurrence.getImage())) {
        return context;
      }

      if (declaration instanceof UnitImportNameDeclaration) {
        // Checks the occurrence against the original unit declaration instead of the import
        // declaration
        checkUnitReference(
            reference.getIdentifier(),
            context,
            occurrence.getImage(),
            (UnitImportNameDeclaration) declaration);
      } else if (!isSpecialCase(declaration, occurrence)) {
        String actual = occurrence.getImage();
        String expected = declaration.getImage();
        if (!actual.equals(expected)) {
          DelphiNode location = reference.getIdentifier();

          context
              .newIssue()
              .onNode(location)
              .withMessage(String.format(MESSAGE, actual, expected))
              .withQuickFixes(
                  QuickFix.newFix(QUICK_FIX_MESSAGE, expected)
                      .withEdit(QuickFixEdit.replace(location, expected)))
              .report();
        }
      }
    }

    return super.visit(reference, context);
  }

  @Override
  public DelphiCheckContext visit(UnitImportNode importNode, DelphiCheckContext context) {
    UnitImportNameDeclaration declaration = importNode.getImportNameDeclaration();

    DelphiNode location = importNode.getNameNode();
    String importName = declaration.fullyQualifiedName();
    checkUnitReference(location, context, importName, declaration);

    return super.visit(importNode, context);
  }

  @Override
  public DelphiCheckContext visit(AttributeNode attributeNode, DelphiCheckContext context) {
    NameReferenceNode reference = attributeNode.getNameReference();
    if (reference == null) {
      return super.visit(attributeNode, context);
    }

    List<NameReferenceNode> nameReferences = reference.flatten();

    for (int i = 0; i + 1 < nameReferences.size(); i++) {
      context = super.visit(nameReferences.get(i), context);
    }

    NameReferenceNode lastNameReference = nameReferences.get(nameReferences.size() - 1);
    NameOccurrence occurrence = lastNameReference.getNameOccurrence();

    if (occurrence != null) {
      NameDeclaration declaration = occurrence.getNameDeclaration();

      if (declaration != null && !isSelf(declaration)) {
        String actual = occurrence.getImage();
        String expected = declaration.getImage();
        if (actual.length() != expected.length()) {
          expected = Strings.CI.removeEnd(expected, "Attribute");
        }

        if (!actual.equals(expected)) {
          DelphiNode location = lastNameReference.getIdentifier();
          context
              .newIssue()
              .onNode(location)
              .withMessage(String.format(MESSAGE, actual, expected))
              .withQuickFixes(
                  QuickFix.newFix(QUICK_FIX_MESSAGE, expected)
                      .withEdit(QuickFixEdit.replace(location, expected)))
              .report();
        }
      }
    }

    ArgumentListNode argumentListNode = attributeNode.getArgumentList();
    if (argumentListNode != null) {
      return super.visit(argumentListNode, context);
    } else {
      return context;
    }
  }

  private static boolean isSpecialCase(NameDeclaration declaration, NameOccurrence occurrence) {
    if (!(declaration instanceof RoutineNameDeclaration)) {
      return false;
    }

    switch (((RoutineNameDeclaration) declaration).fullyQualifiedName()) {
      case "System.WriteLn":
        return occurrence.getImage().equals("Writeln");
      case "System.ReadLn":
        return occurrence.getImage().equals("Readln");
      default:
        return false;
    }
  }

  private static boolean isSelf(NameDeclaration declaration) {
    return declaration instanceof VariableNameDeclaration
        && ((VariableNameDeclaration) declaration).isSelf();
  }

  private void checkUnitReference(
      DelphiNode location,
      DelphiCheckContext context,
      String importName,
      UnitImportNameDeclaration importDeclaration) {
    UnitNameDeclaration originalDeclaration = importDeclaration.getOriginalDeclaration();

    if (originalDeclaration != null) {
      String unitName = originalDeclaration.fullyQualifiedName();

      // Only add violations on import names that are not aliases and do not match the original case
      if (Strings.CI.endsWith(unitName, importName) && !unitName.endsWith(importName)) {
        String matchingSegment = unitName.substring(unitName.length() - importName.length());

        context
            .newIssue()
            .onNode(location)
            .withMessage(String.format(MESSAGE, importName, matchingSegment))
            .withQuickFixes(
                QuickFix.newFix(QUICK_FIX_MESSAGE, matchingSegment)
                    .withEdit(QuickFixEdit.replace(location, matchingSegment)))
            .report();
      }
    }
  }
}
