package org.sonar.plugins.communitydelphi.api.ast;

public interface ParenthesizedExpressionNode extends ExpressionNode {
  ExpressionNode getExpression();
}
