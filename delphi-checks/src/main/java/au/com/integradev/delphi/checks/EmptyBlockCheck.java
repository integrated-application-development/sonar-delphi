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
import org.sonar.plugins.communitydelphi.api.ast.CaseItemStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.CaseStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ElseBlockNode;
import org.sonar.plugins.communitydelphi.api.ast.ExceptBlockNode;
import org.sonar.plugins.communitydelphi.api.ast.ExceptItemNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodBodyNode;
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

  private boolean shouldAddViolation(CompoundStatementNode block) {
    DelphiNode parent = block.getParent();

    if (parent instanceof MethodBodyNode) {
      // Handled by EmptyMethodRule
      return false;
    }

    if (parent instanceof ExceptItemNode) {
      // Handled by SwallowedExceptionsRule
      return false;
    }

    if (parent instanceof CaseItemStatementNode) {
      // Handling all cases in a case statement is a reasonable thing to do.
      // With that being said, a comment is required.
      return block.getComments().isEmpty();
    }

    if (parent instanceof StatementListNode) {
      StatementListNode statementList = (StatementListNode) parent;
      DelphiNode grandparent = parent.getParent();

      if (statementList.getStatements().size() == 1) {
        if (grandparent instanceof ElseBlockNode
            && grandparent.getParent() instanceof CaseStatementNode) {
          // Handling all cases in a case statement is a reasonable thing to do.
          // With that being said, a comment is required.
          return block.getComments().isEmpty();
        }

        // Handled by SwallowedExceptionsRule
        return !(grandparent instanceof ElseBlockNode)
            || !(grandparent.getParent() instanceof ExceptBlockNode);
      }
    }

    return true;
  }
}
