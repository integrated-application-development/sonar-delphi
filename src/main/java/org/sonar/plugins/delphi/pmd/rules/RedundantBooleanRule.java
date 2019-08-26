package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import org.sonar.plugins.delphi.antlr.ast.node.BinaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.BinaryExpressionNode.BinaryOp;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnaryExpressionNode.UnaryOp;

public class RedundantBooleanRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(PrimaryExpressionNode bool, RuleContext data) {
    if (bool.isBooleanLiteral() && (isRedundantComparison(bool) || isNeedlesslyInverted(bool))) {
      addViolation(data, bool);
    }

    return super.visit(bool, data);
  }

  private static boolean isRedundantComparison(PrimaryExpressionNode bool) {
    Node parent = bool.findParentheses().jjtGetParent();

    if (parent instanceof BinaryExpressionNode) {
      BinaryOp op = ((BinaryExpressionNode) parent).getOperator();
      return op == BinaryOp.EQUAL || op == BinaryOp.NOT_EQUAL;
    }

    return false;
  }

  private static boolean isNeedlesslyInverted(PrimaryExpressionNode bool) {
    Node parent = bool.findParentheses().jjtGetParent();
    return parent instanceof UnaryExpressionNode
        && ((UnaryExpressionNode) parent).getOperator() == UnaryOp.NOT;
  }
}
