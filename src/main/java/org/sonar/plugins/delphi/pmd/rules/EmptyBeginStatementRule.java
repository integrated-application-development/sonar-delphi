package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import org.sonar.plugins.delphi.antlr.ast.node.CompoundStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ElseBlockNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExceptBlockNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExceptItemNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodBodyNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementListNode;

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

    if (parent instanceof StatementListNode) {
      StatementListNode statementList = (StatementListNode) parent;
      parent = parent.jjtGetParent();

      // Handled by SwallowedExceptionsRule
      return !(statementList.getStatements().size() == 1
          && parent instanceof ElseBlockNode
          && parent.jjtGetParent() instanceof ExceptBlockNode);
    }

    return true;
  }
}
