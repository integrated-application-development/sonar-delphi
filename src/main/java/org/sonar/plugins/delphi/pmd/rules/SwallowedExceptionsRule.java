package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.CompoundStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExceptBlockNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExceptItemNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementListNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementNode;

/**
 * Any case where an except block or an exception handler is empty means that any raised exception
 * is silently swallowed.
 */
public class SwallowedExceptionsRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(ExceptBlockNode exceptBlock, RuleContext data) {
    StatementListNode statementList = exceptBlock.getStatementList();
    if (statementList != null && statementList.isEmpty()) {
      addViolation(data, exceptBlock);
    }

    return super.visit(exceptBlock, data);
  }

  @Override
  public RuleContext visit(ExceptItemNode handler, RuleContext data) {
    StatementNode statement = handler.getStatement();
    if (statement == null || isEmptyCompoundStatement(statement)) {
      addViolation(data, handler);
    }

    return super.visit(handler, data);
  }

  private static boolean isEmptyCompoundStatement(StatementNode statement) {
    return statement instanceof CompoundStatementNode
        && ((CompoundStatementNode) statement).isEmpty();
  }
}
