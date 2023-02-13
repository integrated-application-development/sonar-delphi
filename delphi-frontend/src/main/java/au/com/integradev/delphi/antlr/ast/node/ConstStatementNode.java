package au.com.integradev.delphi.antlr.ast.node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ConstStatementNode extends StatementNode {
  NameDeclarationNode getNameDeclarationNode();

  @Nullable
  TypeNode getTypeNode();

  @Nonnull
  ExpressionNode getExpression();
}
