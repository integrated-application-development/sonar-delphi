/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ImplementationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.UnitImportNode;
import org.sonar.plugins.communitydelphi.api.ast.UsesClauseNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFix;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFixEdit;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "ImportSpecificityRule", repositoryKey = "delph")
@Rule(key = "ImportSpecificity")
public class ImportSpecificityCheck extends AbstractImportCheck {
  @Override
  protected String getIssueMessage() {
    return "Move this import to the implementation section.";
  }

  @Override
  public DelphiCheckContext visit(ImplementationSectionNode section, DelphiCheckContext context) {
    return context;
  }

  @Override
  protected boolean isViolation(UnitImportNode unitImport) {
    UnitNameDeclaration dependency = unitImport.getImportNameDeclaration().getOriginalDeclaration();
    return !getUnitDeclaration().getInterfaceDependencies().contains(dependency)
        && getUnitDeclaration().getImplementationDependencies().contains(dependency);
  }

  private static FilePosition getTokenEnd(DelphiNode node) {
    DelphiToken token = node.getToken();
    return FilePosition.from(
        token.getEndLine(), token.getEndColumn(), token.getEndLine(), token.getEndColumn());
  }

  @Override
  protected QuickFix getQuickFix(UnitImportNode unitImport) {
    var implementation =
        unitImport.getAst().getFirstDescendantOfType(ImplementationSectionNode.class);
    if (implementation == null) {
      return null;
    }

    UsesClauseNode implementationUses = implementation.getUsesClause();
    if (implementationUses == null) {
      FilePosition afterImplementation = getTokenEnd(implementation);

      return QuickFix.newFix("Move to implementation section")
          .withEdits(
              deleteImportEdit(unitImport),
              QuickFixEdit.insert(
                  "\r\n\r\nuses ",
                  afterImplementation.getBeginLine(),
                  afterImplementation.getBeginColumn()),
              QuickFixEdit.copy(unitImport, afterImplementation),
              QuickFixEdit.insert(
                  ";", afterImplementation.getBeginLine(), afterImplementation.getBeginColumn()));
    }

    return buildMoveQuickFix(unitImport, implementationUses);
  }

  private QuickFix buildMoveQuickFix(
      UnitImportNode unitImport, UsesClauseNode destinationUsesClause) {
    List<UnitImportNode> imports = destinationUsesClause.getImports();
    if (imports.isEmpty()) {
      return null;
    }

    var lastImplementationImport = imports.get(imports.size() - 1);

    return QuickFix.newFix("Move to implementation section")
        .withEdits(
            deleteImportEdit(unitImport),
            QuickFixEdit.insertAfter(", ", lastImplementationImport),
            QuickFixEdit.copyAfter(unitImport, lastImplementationImport));
  }
}
