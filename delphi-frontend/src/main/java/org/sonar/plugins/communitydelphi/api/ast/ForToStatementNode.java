package org.sonar.plugins.communitydelphi.api.ast;

public interface ForToStatementNode extends ForStatementNode {
  ExpressionNode getInitializerExpression();

  ExpressionNode getTargetExpression();
}
