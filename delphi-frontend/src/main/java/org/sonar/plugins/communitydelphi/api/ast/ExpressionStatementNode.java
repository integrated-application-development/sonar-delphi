package org.sonar.plugins.communitydelphi.api.ast;

public interface ExpressionStatementNode extends StatementNode {
  ExpressionNode getExpression();
}
