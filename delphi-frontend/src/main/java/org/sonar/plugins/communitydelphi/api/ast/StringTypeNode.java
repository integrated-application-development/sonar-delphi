package org.sonar.plugins.communitydelphi.api.ast;

public interface StringTypeNode extends TypeNode {

  default boolean isFixedString() {
    return getChild(0) instanceof ExpressionNode;
  }
}
