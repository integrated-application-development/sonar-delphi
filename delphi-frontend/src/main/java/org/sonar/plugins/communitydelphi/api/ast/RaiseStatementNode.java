package org.sonar.plugins.communitydelphi.api.ast;

import javax.annotation.Nullable;

public interface RaiseStatementNode extends StatementNode {
  @Nullable
  ExpressionNode getRaiseExpression();
}
