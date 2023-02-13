package org.sonar.plugins.communitydelphi.api.ast;

public interface RangeExpressionNode extends ExpressionNode {
  ExpressionNode getLowExpression();

  ExpressionNode getHighExpression();
}
