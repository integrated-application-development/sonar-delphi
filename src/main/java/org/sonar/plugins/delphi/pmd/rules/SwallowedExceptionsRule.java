package org.sonar.plugins.delphi.pmd.rules;

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.CompoundStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ElseBlockNode;
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
    if (isEmptyExcept(exceptBlock)) {
      addViolation(data, exceptBlock);
    } else if (isEmptyElse(exceptBlock)) {
      addViolation(data, exceptBlock.getElseBlock());
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

  private static boolean isEmptyExcept(ExceptBlockNode exceptBlock) {
    StatementListNode statementList = exceptBlock.getStatementList();
    return statementList != null && statementList.isEmpty();
  }

  private static boolean isEmptyElse(ExceptBlockNode exceptBlock) {
    ElseBlockNode elseBlock = exceptBlock.getElseBlock();
    if (elseBlock == null) {
      return false;
    }

    List<StatementNode> statements = elseBlock.getStatementList().getStatements();
    if (statements.isEmpty()) {
      return true;
    } else if (statements.size() == 1) {
      StatementNode statement = statements.get(0);
      return statement instanceof CompoundStatementNode
          && ((CompoundStatementNode) statement).isEmpty();
    }

    return false;
  }

  private static boolean isEmptyCompoundStatement(StatementNode statement) {
    return statement instanceof CompoundStatementNode
        && ((CompoundStatementNode) statement).isEmpty();
  }
}
