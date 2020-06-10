package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.BinaryExpressionNode;
import org.sonar.plugins.delphi.operator.BinaryOperator;

public class AssignedNilCheckRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(BinaryExpressionNode expression, RuleContext data) {
    BinaryOperator operator = expression.getOperator();
    if ((operator == BinaryOperator.EQUAL || operator == BinaryOperator.NOT_EQUAL)
        && (expression.getLeft().isNilLiteral() || expression.getRight().isNilLiteral())) {
      addViolation(data, expression);
    }
    return super.visit(expression, data);
  }
}
