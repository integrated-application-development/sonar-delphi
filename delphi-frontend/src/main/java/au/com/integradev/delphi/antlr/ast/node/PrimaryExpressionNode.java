package au.com.integradev.delphi.antlr.ast.node;

public interface PrimaryExpressionNode extends ExpressionNode {
  boolean isInheritedCall();

  boolean isBareInherited();
}
