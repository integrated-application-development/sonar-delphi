package org.sonar.plugins.communitydelphi.api.ast;

import javax.annotation.Nullable;

public interface VarStatementNode extends StatementNode {
  NameDeclarationListNode getNameDeclarationList();

  @Nullable
  TypeNode getTypeNode();

  @Nullable
  ExpressionNode getExpression();
}
