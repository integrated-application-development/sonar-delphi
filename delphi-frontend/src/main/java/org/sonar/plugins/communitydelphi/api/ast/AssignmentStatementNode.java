package org.sonar.plugins.communitydelphi.api.ast;

public interface AssignmentStatementNode extends StatementNode {
  ExpressionNode getAssignee();

  ExpressionNode getValue();
}
