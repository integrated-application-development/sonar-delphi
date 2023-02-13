package au.com.integradev.delphi.antlr.ast.node;

import org.jetbrains.annotations.Nullable;

public interface IfStatementNode extends StatementNode {
  ExpressionNode getGuardExpression();

  boolean hasElseBranch();

  @Nullable
  StatementNode getThenStatement();

  @Nullable
  StatementNode getElseStatement();
}
