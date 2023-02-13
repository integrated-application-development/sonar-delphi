package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.operator.UnaryOperator;

public interface UnaryExpressionNode extends ExpressionNode {
  UnaryOperator getOperator();

  ExpressionNode getOperand();
}
