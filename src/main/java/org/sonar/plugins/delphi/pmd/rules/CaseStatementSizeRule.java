package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.CaseStatementNode;

public class CaseStatementSizeRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(CaseStatementNode caseStatement, RuleContext data) {
    int branchCount = caseStatement.getCaseItems().size();
    if (caseStatement.getElseBlockNode() != null) {
      ++branchCount;
    }

    if (branchCount < 3) {
      addViolation(data, caseStatement.jjtGetChild(0));
    }

    return super.visit(caseStatement, data);
  }
}
