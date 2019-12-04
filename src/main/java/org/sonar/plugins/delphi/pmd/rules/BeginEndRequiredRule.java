package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import org.sonar.plugins.delphi.antlr.ast.node.CaseItemStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.CompoundStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.IfStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodBodyNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementListNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementNode;

public class BeginEndRequiredRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(StatementNode statement, RuleContext data) {
    if (isMissingBeginEnd(statement)) {
      addViolation(data, statement);
    }
    return super.visit(statement, data);
  }

  private static boolean isMissingBeginEnd(StatementNode statement) {
    if (statement.jjtGetParent() instanceof MethodBodyNode) {
      return false;
    }

    if (statement instanceof CompoundStatementNode || statement instanceof CaseItemStatementNode) {
      return false;
    }

    Node parent = statement.jjtGetParent();

    if (statement instanceof IfStatementNode && parent instanceof IfStatementNode) {
      return ((IfStatementNode) parent).getElseStatement() != statement;
    }

    return !(parent instanceof StatementListNode);
  }
}
