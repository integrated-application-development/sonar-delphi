package au.com.integradev.delphi.antlr.ast.node;

import javax.annotation.Nullable;

public interface RaiseStatementNode extends StatementNode {
  @Nullable
  ExpressionNode getRaiseExpression();
}
