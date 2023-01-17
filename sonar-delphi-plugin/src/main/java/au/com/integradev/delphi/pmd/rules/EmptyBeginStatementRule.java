/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package au.com.integradev.delphi.pmd.rules;

import au.com.integradev.delphi.antlr.ast.node.CaseItemStatementNode;
import au.com.integradev.delphi.antlr.ast.node.CaseStatementNode;
import au.com.integradev.delphi.antlr.ast.node.CompoundStatementNode;
import au.com.integradev.delphi.antlr.ast.node.ElseBlockNode;
import au.com.integradev.delphi.antlr.ast.node.ExceptBlockNode;
import au.com.integradev.delphi.antlr.ast.node.ExceptItemNode;
import au.com.integradev.delphi.antlr.ast.node.MethodBodyNode;
import au.com.integradev.delphi.antlr.ast.node.StatementListNode;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;

public class EmptyBeginStatementRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(CompoundStatementNode block, RuleContext data) {
    if (block.isEmpty() && shouldAddViolation(block)) {
      addViolation(data, block);
    }

    return super.visit(block, data);
  }

  private boolean shouldAddViolation(CompoundStatementNode block) {
    Node parent = block.jjtGetParent();

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
      Node grandparent = parent.jjtGetParent();

      if (statementList.getStatements().size() == 1) {
        if (grandparent instanceof ElseBlockNode
            && grandparent.jjtGetParent() instanceof CaseStatementNode) {
          // Handling all cases in a case statement is a reasonable thing to do.
          // With that being said, a comment is required.
          return block.getComments().isEmpty();
        }

        // Handled by SwallowedExceptionsRule
        return !(grandparent instanceof ElseBlockNode)
            || !(grandparent.jjtGetParent() instanceof ExceptBlockNode);
      }
    }

    return true;
  }
}
