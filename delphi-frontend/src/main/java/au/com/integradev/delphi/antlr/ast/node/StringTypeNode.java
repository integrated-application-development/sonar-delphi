package au.com.integradev.delphi.antlr.ast.node;

public interface StringTypeNode extends TypeNode {

  default boolean isFixedString() {
    return jjtGetChild(0) instanceof ExpressionNode;
  }
}
