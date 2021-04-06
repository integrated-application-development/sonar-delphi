package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class ConstStatementNode extends StatementNode {
  public ConstStatementNode(Token token) {
    super(token);
  }

  public NameDeclarationNode getNameDeclarationNode() {
    return (NameDeclarationNode) jjtGetChild(0);
  }

  @Nullable
  public TypeNode getTypeNode() {
    return getFirstChildOfType(TypeNode.class);
  }

  @NotNull
  public ExpressionNode getExpression() {
    return getFirstChildOfType(ExpressionNode.class);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }
}
