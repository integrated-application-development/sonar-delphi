package org.sonar.plugins.communitydelphi.api.ast;

import org.jetbrains.annotations.Nullable;

public interface IfStatementNode extends StatementNode {
  ExpressionNode getGuardExpression();

  boolean hasElseBranch();

  @Nullable
  StatementNode getThenStatement();

  @Nullable
  StatementNode getElseStatement();
}
