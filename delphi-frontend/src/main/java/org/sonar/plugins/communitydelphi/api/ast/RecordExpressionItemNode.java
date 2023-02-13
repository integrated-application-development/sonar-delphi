package org.sonar.plugins.communitydelphi.api.ast;

public interface RecordExpressionItemNode extends DelphiNode {
  IdentifierNode getIdentifier();

  ExpressionNode getExpression();
}
