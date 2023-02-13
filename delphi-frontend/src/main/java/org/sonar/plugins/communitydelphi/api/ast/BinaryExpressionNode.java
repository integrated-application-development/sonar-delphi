package org.sonar.plugins.communitydelphi.api.ast;

import au.com.integradev.delphi.operator.BinaryOperator;

public interface BinaryExpressionNode extends ExpressionNode {
  ExpressionNode getLeft();

  ExpressionNode getRight();

  BinaryOperator getOperator();
}
