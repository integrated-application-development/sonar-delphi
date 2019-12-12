package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.node.StatementListNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementNode;

public class SuperfluousSemicolonsRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(StatementListNode statementList, RuleContext data) {
    Node previous = null;
    for (int i = 0; i < statementList.jjtGetNumChildren(); ++i) {
      Node current = statementList.jjtGetChild(i);
      if (current.jjtGetId() == DelphiLexer.SEMI && !(previous instanceof StatementNode)) {
        addViolation(data, current);
      }
      previous = current;
    }
    return super.visit(statementList, data);
  }
}
