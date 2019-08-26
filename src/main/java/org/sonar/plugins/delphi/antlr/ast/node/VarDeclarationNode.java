package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class VarDeclarationNode extends DelphiNode {
  public VarDeclarationNode(Token token) {
    super(token);
  }

  public VarDeclarationNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public IdentifierListNode getIdentifierList() {
    return (IdentifierListNode) jjtGetChild(0);
  }

  public VarSectionNode getVarSection() {
    return (VarSectionNode) jjtGetParent();
  }
}
