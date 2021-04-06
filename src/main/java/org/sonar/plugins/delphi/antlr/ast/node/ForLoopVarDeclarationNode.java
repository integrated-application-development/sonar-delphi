package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class ForLoopVarDeclarationNode extends ForLoopVarNode {
  public ForLoopVarDeclarationNode(Token token) {
    super(token);
  }

  public ForLoopVarDeclarationNode(int tokenType) {
    super(tokenType);
  }

  public NameDeclarationNode getNameDeclarationNode() {
    return (NameDeclarationNode) jjtGetChild(0);
  }

  @Nullable
  public TypeNode getTypeNode() {
    return (TypeNode) jjtGetChild(1);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }
}
