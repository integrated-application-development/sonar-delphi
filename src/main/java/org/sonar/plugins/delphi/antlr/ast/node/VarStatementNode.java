package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class VarStatementNode extends StatementNode {
  public VarStatementNode(Token token) {
    super(token);
  }

  public NameDeclarationListNode getNameDeclarationList() {
    return (NameDeclarationListNode) jjtGetChild(0);
  }

  @Nullable
  public TypeNode getTypeNode() {
    return getFirstChildOfType(TypeNode.class);
  }

  @Nullable
  public ExpressionNode getExpression() {
    return getFirstChildOfType(ExpressionNode.class);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }
}
