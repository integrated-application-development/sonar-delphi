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
import org.sonar.plugins.communitydelphi.api.ast.AnonymousMethodNode;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ElseBlockNode;
import org.sonar.plugins.communitydelphi.api.ast.ExceptBlockNode;
import org.sonar.plugins.communitydelphi.api.ast.ExceptItemNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineBodyNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementListNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "EmptyBeginStatementRule", repositoryKey = "delph")
@Rule(key = "EmptyBlock")
public class EmptyBlockCheck extends DelphiCheck {
  private static final String MESSAGE = "Either remove or fill this block of code.";

  @Override
  public DelphiCheckContext visit(CompoundStatementNode block, DelphiCheckContext context) {
    if (block.isEmpty() && shouldAddViolation(block)) {
      reportIssue(context, block, MESSAGE);
    }

    return super.visit(block, context);
  }

  private static boolean shouldAddViolation(CompoundStatementNode block) {
    DelphiNode parent = block.getParent();

    if (!block.getComments().isEmpty()) {
      // An empty block is OK if it has an explanatory comment.
      return false;
    }

    if (parent instanceof RoutineBodyNode || parent instanceof AnonymousMethodNode) {
      // Handled by EmptyRoutine
      return false;
    }

    if (parent instanceof ExceptItemNode) {
      // Handled by SwallowedException
      return false;
    }

    if (parent instanceof StatementListNode) {
      StatementListNode statementList = (StatementListNode) parent;
      DelphiNode enclosing = statementList.getParent();

      if (statementList.getStatements().size() == 1) {
        if (enclosing instanceof ElseBlockNode) {
          enclosing = enclosing.getParent();
        }
        // Handled by SwallowedException
        return !(enclosing instanceof ExceptBlockNode);
      }
    }

    return true;
  }
}
