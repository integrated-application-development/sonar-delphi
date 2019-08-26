package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class ConstDeclarationNode extends DelphiNode {
  public ConstDeclarationNode(Token token) {
    super(token);
  }

  public ConstDeclarationNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public IdentifierNode getIdentifier() {
    return (IdentifierNode) jjtGetChild(0);
  }

  public ExpressionNode getExpression() {
    return (ExpressionNode) jjtGetChild(1);
  }

  public TypeNode getTypeNode() {
    return (TypeNode) jjtGetChild(2);
  }
}
