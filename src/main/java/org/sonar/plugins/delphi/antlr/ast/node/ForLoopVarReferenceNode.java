package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class ForLoopVarReferenceNode extends ForLoopVarNode {
  public ForLoopVarReferenceNode(Token token) {
    super(token);
  }

  public ForLoopVarReferenceNode(int tokenType) {
    super(tokenType);
  }

  public NameReferenceNode getNameReference() {
    return (NameReferenceNode) jjtGetChild(0);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }
}
