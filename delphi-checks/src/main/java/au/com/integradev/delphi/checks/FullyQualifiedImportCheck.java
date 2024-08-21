/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.UnitImportNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFix;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFixEdit;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitImportNameDeclaration;

@Rule(key = "FullyQualifiedImport")
public class FullyQualifiedImportCheck extends DelphiCheck {

  @Override
  public DelphiCheckContext visit(UnitImportNode unitImportNode, DelphiCheckContext context) {
    if (!unitImportNode.isResolvedImport()) {
      return context;
    }

    UnitImportNameDeclaration unitImportNameDeclaration = unitImportNode.getImportNameDeclaration();

    String unitFullyQualifiedName = unitImportNameDeclaration.getOriginalDeclaration().getImage();
    String unitImportName = unitImportNameDeclaration.getImage();

    if (unitImportName.length() != unitFullyQualifiedName.length()) {
      context
          .newIssue()
          .onNode(unitImportNode)
          .withMessage(
              "Fully qualify this unit name (found: \"%s\" expected: \"%s\").",
              unitImportName, unitFullyQualifiedName)
          .withQuickFixes(
              QuickFix.newFix("Fully qualify unit import")
                  .withEdit(
                      QuickFixEdit.replace(unitImportNode.getNameNode(), unitFullyQualifiedName)))
          .report();
    }

    return context;
  }
}
