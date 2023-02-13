package au.com.integradev.delphi.antlr.ast.node;

public interface ParenthesizedExpressionNode extends ExpressionNode {
  ExpressionNode getExpression();
}
