package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.antlr.ast.node.BinaryExpressionNode.BinaryOp.EQUAL;
import static org.sonar.plugins.delphi.antlr.ast.node.BinaryExpressionNode.BinaryOp.NOT_EQUAL;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.BinaryExpressionNode;

public class AssignedNilCheckRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(BinaryExpressionNode expression, RuleContext data) {
    if ((expression.getOperator() == EQUAL || expression.getOperator() == NOT_EQUAL)
        && (expression.getLeft().isNilLiteral() || expression.getRight().isNilLiteral())) {
      addViolation(data, expression);
    }
    return super.visit(expression, data);
  }
}
