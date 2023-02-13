package au.com.integradev.delphi.antlr.ast.node;

public interface AssignmentStatementNode extends StatementNode {
  ExpressionNode getAssignee();

  ExpressionNode getValue();
}
