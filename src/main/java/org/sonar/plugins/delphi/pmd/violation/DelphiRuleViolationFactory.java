package org.sonar.plugins.delphi.pmd.violation;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.AbstractRuleViolationFactory;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.pmd.FilePosition;
import org.sonar.plugins.delphi.pmd.rules.DelphiRule;

public class DelphiRuleViolationFactory extends AbstractRuleViolationFactory {
  @Override
  protected RuleViolation createRuleViolation(
      Rule rule, RuleContext ruleContext, Node node, String message) {
    return ((DelphiRule) rule)
        .newViolation(ruleContext)
        .atPosition(FilePosition.from((DelphiNode) node))
        .atLocation((DelphiNode) node)
        .message(message)
        .build();
  }

  @Override
  protected RuleViolation createRuleViolation(
      Rule rule, RuleContext ruleContext, Node node, String message, int beginLine, int endLine) {
    return ((DelphiRule) rule)
        .newViolation(ruleContext)
        .atPosition(FilePosition.atLineLevel(beginLine, endLine))
        .atLocation((DelphiNode) node)
        .message(message)
        .build();
  }
}
