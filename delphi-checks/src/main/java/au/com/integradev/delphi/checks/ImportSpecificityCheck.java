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
import org.sonar.plugins.communitydelphi.api.ast.ImplementationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.UnitImportNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;
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
}
