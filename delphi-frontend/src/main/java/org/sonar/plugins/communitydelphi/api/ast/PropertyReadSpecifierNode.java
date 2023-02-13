package org.sonar.plugins.communitydelphi.api.ast;

public interface PropertyReadSpecifierNode extends DelphiNode {
  PrimaryExpressionNode getExpression();
}
