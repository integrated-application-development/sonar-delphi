package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.ParenthesizedExpressionNode;

public class RedundantParenthesesRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(ParenthesizedExpressionNode expression, RuleContext data) {
    if (expression.jjtGetParent() instanceof ParenthesizedExpressionNode) {
      addViolation(data, expression.jjtGetChild(0));
    }
    return super.visit(expression, data);
  }
}
