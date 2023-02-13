package au.com.integradev.delphi.antlr.ast.node;

public interface ForToStatementNode extends ForStatementNode {
  ExpressionNode getInitializerExpression();

  ExpressionNode getTargetExpression();
}
