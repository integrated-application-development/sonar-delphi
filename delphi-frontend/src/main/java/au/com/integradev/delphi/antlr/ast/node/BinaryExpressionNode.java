package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.operator.BinaryOperator;

public interface BinaryExpressionNode extends ExpressionNode {
  ExpressionNode getLeft();

  ExpressionNode getRight();

  BinaryOperator getOperator();
}
