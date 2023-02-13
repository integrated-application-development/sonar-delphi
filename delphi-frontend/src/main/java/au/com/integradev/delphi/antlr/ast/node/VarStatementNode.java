package au.com.integradev.delphi.antlr.ast.node;

import javax.annotation.Nullable;

public interface VarStatementNode extends StatementNode {
  NameDeclarationListNode getNameDeclarationList();

  @Nullable
  TypeNode getTypeNode();

  @Nullable
  ExpressionNode getExpression();
}
