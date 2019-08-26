package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class MethodReturnTypeNode extends DelphiNode {
  public MethodReturnTypeNode(Token token) {
    super(token);
  }

  public MethodReturnTypeNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public TypeNode getTypeNode() {
    return (TypeNode) jjtGetChild(0);
  }
}
