package org.sonar.plugins.communitydelphi.api.ast;

public interface SubRangeTypeNode extends TypeNode {
  ExpressionNode getLowExpression();

  ExpressionNode getHighExpression();
}
