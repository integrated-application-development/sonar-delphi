package org.sonar.plugins.communitydelphi.api.ast;

import au.com.integradev.delphi.operator.UnaryOperator;

public interface UnaryExpressionNode extends ExpressionNode {
  UnaryOperator getOperator();

  ExpressionNode getOperand();
}
