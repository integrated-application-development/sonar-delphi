package org.sonar.plugins.communitydelphi.api.ast;

public interface PropertyWriteSpecifierNode extends DelphiNode {
  PrimaryExpressionNode getExpression();
}
