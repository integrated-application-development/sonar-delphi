package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.operator.UnaryOperator;

public interface UnaryExpressionNode extends ExpressionNode {
  UnaryOperator getOperator();

  ExpressionNode getOperand();
}
