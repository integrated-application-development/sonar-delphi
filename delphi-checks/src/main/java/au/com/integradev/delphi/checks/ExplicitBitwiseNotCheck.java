/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.UnaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import org.sonar.plugins.communitydelphi.api.operator.BinaryOperator;
import org.sonar.plugins.communitydelphi.api.operator.UnaryOperator;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFix;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFixEdit;

@Rule(key = "ExplicitBitwiseNot")
public class ExplicitBitwiseNotCheck extends DelphiCheck {
  @Override
  public DelphiCheckContext visit(BinaryExpressionNode node, DelphiCheckContext context) {
    if (node.getOperator() == BinaryOperator.IN) {
      checkBitwiseNot(node.getLeft(), context);
      checkBitwiseNot(node.getRight(), context);
    }

    return super.visit(node, context);
  }

  private void checkBitwiseNot(ExpressionNode node, DelphiCheckContext context) {
    if (!(node instanceof UnaryExpressionNode)) {
      return;
    }
    UnaryExpressionNode unaryNode = (UnaryExpressionNode) node;

    if (isBitwiseNot(unaryNode)) {
      context
          .newIssue()
          .onFilePosition(FilePosition.from(unaryNode.getToken()))
          .withMessage("Parenthesize this bitwise 'not' operation.")
          .withQuickFixes(
              QuickFix.newFix("Parenthesize bitwise 'not'")
                  .withEdits(
                      QuickFixEdit.insertBefore("(", node), QuickFixEdit.insertAfter(")", node)))
          .report();
    }
  }

  private boolean isBitwiseNot(UnaryExpressionNode node) {
    return node.getOperator() == UnaryOperator.NOT && node.getType().isInteger();
  }
}
