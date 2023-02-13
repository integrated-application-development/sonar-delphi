package org.sonar.plugins.communitydelphi.api.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ConstStatementNode extends StatementNode {
  NameDeclarationNode getNameDeclarationNode();

  @Nullable
  TypeNode getTypeNode();

  @Nonnull
  ExpressionNode getExpression();
}
