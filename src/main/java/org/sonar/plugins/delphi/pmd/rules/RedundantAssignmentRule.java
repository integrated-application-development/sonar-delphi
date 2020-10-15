package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.AssignmentStatementNode;

public class RedundantAssignmentRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(AssignmentStatementNode assignment, RuleContext data) {
    String assignee = assignment.getAssignee().skipParentheses().getImage();
    String value = assignment.getValue().skipParentheses().getImage();
    if (assignee.equalsIgnoreCase(value)) {
      addViolation(data, assignment);
    }
    return data;
  }
}
