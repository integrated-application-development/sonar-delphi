package au.com.integradev.delphi.antlr.ast.node;

public interface RangeExpressionNode extends ExpressionNode {
  ExpressionNode getLowExpression();

  ExpressionNode getHighExpression();
}
