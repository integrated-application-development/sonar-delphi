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

import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationListNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.reporting.DelphiIssueBuilder;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFix;

public abstract class AbstractGroupedDeclarationCheck extends DelphiCheck {
  protected abstract boolean isRelevantDeclarationList(NameDeclarationListNode declarationList);

  protected abstract String getIssueMessage();

  @Override
  public DelphiCheckContext visit(
      NameDeclarationListNode declarationList, DelphiCheckContext context) {
    if (isRelevantDeclarationList(declarationList)
        && declarationList.getDeclarations().size() > 1) {
      DelphiIssueBuilder newIssue =
          context.newIssue().onNode(declarationList).withMessage(getIssueMessage());

      QuickFix quickFix = createQuickFix(declarationList, context);
      if (quickFix != null) {
        newIssue.withQuickFixes(quickFix);
      }

      newIssue.report();
    }
    return super.visit(declarationList, context);
  }

  protected QuickFix createQuickFix(
      NameDeclarationListNode declarationList, DelphiCheckContext context) {
    return null;
  }
}
