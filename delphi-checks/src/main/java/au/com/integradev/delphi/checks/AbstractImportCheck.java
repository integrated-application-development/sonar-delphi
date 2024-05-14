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

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.FileHeaderNode;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.UnitImportNode;
import org.sonar.plugins.communitydelphi.api.ast.UsesClauseNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import org.sonar.plugins.communitydelphi.api.reporting.DelphiIssueBuilder;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFix;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFixEdit;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;

public abstract class AbstractImportCheck extends DelphiCheck {
  @RuleProperty(
      key = "exclusions",
      description =
          "Comma-delimited fully-qualified names of unit imports to exclude"
              + "(only in the interface section)")
  public String exclusions = "";

  private Set<String> exclusionSet;
  private UnitNameDeclaration unitDeclaration;

  protected abstract String getIssueMessage();

  @Override
  public void start(DelphiCheckContext context) {
    exclusionSet = Set.copyOf(Splitter.on(',').trimResults().splitToList(exclusions));
  }

  @Override
  public DelphiCheckContext visit(DelphiAst ast, DelphiCheckContext context) {
    unitDeclaration = null;
    if (ast.isUnit()) {
      return super.visit(ast, context);
    } else {
      return context;
    }
  }

  @Override
  public DelphiCheckContext visit(FileHeaderNode headerNode, DelphiCheckContext context) {
    unitDeclaration = (UnitNameDeclaration) headerNode.getNameNode().getNameDeclaration();
    return context;
  }

  @Override
  public DelphiCheckContext visit(UnitImportNode unitImport, DelphiCheckContext context) {
    if (!isExcluded(unitImport) && isViolation(unitImport)) {
      DelphiIssueBuilder builder =
          context.newIssue().onNode(unitImport).withMessage(getIssueMessage());

      var quickFix = getQuickFix(unitImport);
      if (quickFix != null) {
        builder.withQuickFixes(quickFix);
      }

      builder.report();
    }
    return context;
  }

  private boolean isExcluded(UnitImportNode unitImport) {
    if (!unitImport.isResolvedImport()) {
      return true;
    }

    if (unitImport.getFirstParentOfType(InterfaceSectionNode.class) != null) {
      UnitImportNameDeclaration importDeclaration = unitImport.getImportNameDeclaration();
      UnitNameDeclaration dependency = importDeclaration.getOriginalDeclaration();
      Objects.requireNonNull(dependency);
      return exclusionSet.contains(dependency.getImage());
    }

    return false;
  }

  protected UnitNameDeclaration getUnitDeclaration() {
    Preconditions.checkNotNull(unitDeclaration);
    return unitDeclaration;
  }

  protected abstract boolean isViolation(UnitImportNode unitImport);

  protected QuickFix getQuickFix(UnitImportNode unitImport) {
    return null;
  }

  protected QuickFixEdit deleteImportEdit(UnitImportNode unitImport) {
    UsesClauseNode usesClause = (UsesClauseNode) unitImport.getParent();
    List<UnitImportNode> imports = usesClause.getImports();

    if (imports.size() == 1) {
      return QuickFixEdit.delete(usesClause);
    }

    int index = imports.indexOf(unitImport);
    if (index == 0) {
      return QuickFixEdit.delete(
          FilePosition.from(
              unitImport.getBeginLine(),
              unitImport.getBeginColumn(),
              imports.get(1).getBeginLine(),
              imports.get(1).getBeginColumn()));
    }

    return QuickFixEdit.delete(
        FilePosition.from(
            imports.get(index - 1).getEndLine(),
            imports.get(index - 1).getEndColumn(),
            unitImport.getEndLine(),
            unitImport.getEndColumn()));
  }
}
