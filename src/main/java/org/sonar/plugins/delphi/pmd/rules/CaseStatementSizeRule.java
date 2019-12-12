package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.CaseStatementNode;

public class CaseStatementSizeRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(CaseStatementNode caseStatement, RuleContext data) {
    if (caseStatement.getCaseItems().size() < 2) {
      addViolation(data, caseStatement.jjtGetChild(0));
    }

    return super.visit(caseStatement, data);
  }
}
