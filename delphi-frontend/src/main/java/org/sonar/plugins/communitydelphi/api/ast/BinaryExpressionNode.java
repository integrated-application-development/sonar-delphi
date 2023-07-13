package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.operator.BinaryOperator;

public interface BinaryExpressionNode extends ExpressionNode {
  ExpressionNode getLeft();

  ExpressionNode getRight();

  BinaryOperator getOperator();
}
