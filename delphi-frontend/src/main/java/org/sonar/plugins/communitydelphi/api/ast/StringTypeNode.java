package org.sonar.plugins.communitydelphi.api.ast;

public interface StringTypeNode extends TypeNode {

  default boolean isFixedString() {
    return jjtGetChild(0) instanceof ExpressionNode;
  }
}
