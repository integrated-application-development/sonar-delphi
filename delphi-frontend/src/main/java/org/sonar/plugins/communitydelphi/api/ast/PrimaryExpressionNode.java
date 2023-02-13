package org.sonar.plugins.communitydelphi.api.ast;

public interface PrimaryExpressionNode extends ExpressionNode {
  boolean isInheritedCall();

  boolean isBareInherited();
}
