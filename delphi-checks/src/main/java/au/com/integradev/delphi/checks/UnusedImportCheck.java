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

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.UnitImportNode;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFix;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "UnusedImportsRule", repositoryKey = "delph")
@Rule(key = "UnusedImport")
public class UnusedImportCheck extends AbstractImportCheck {
  @Override
  protected boolean isViolation(UnitImportNode unitImport) {
    UnitNameDeclaration dependency = unitImport.getImportNameDeclaration().getOriginalDeclaration();
    return !getUnitDeclaration().hasDependency(dependency);
  }

  @Override
  protected String getIssueMessage() {
    return "Review this potentially unnecessary import.";
  }

  @Override
  protected QuickFix getQuickFix(UnitImportNode unitImport) {
    return QuickFix.newFix("Remove unused import").withEdit(deleteImportEdit(unitImport));
  }
}
