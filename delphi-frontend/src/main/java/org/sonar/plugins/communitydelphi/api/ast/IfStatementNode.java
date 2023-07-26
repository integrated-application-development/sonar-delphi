package org.sonar.plugins.communitydelphi.api.ast;

import javax.annotation.Nullable;

public interface IfStatementNode extends StatementNode {
  ExpressionNode getGuardExpression();

  boolean hasElseBranch();

  @Nullable
  StatementNode getThenStatement();

  @Nullable
  StatementNode getElseStatement();
}
