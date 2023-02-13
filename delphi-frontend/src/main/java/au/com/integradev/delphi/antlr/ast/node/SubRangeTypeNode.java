package au.com.integradev.delphi.antlr.ast.node;

public interface SubRangeTypeNode extends TypeNode {
  ExpressionNode getLowExpression();

  ExpressionNode getHighExpression();
}
