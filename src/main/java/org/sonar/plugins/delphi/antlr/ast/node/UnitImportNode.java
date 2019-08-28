package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class UnitImportNode extends DelphiNode {
  public UnitImportNode(Token token) {
    super(token);
  }

  public UnitImportNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public NameDeclarationNode getNameDeclaration() {
    return (NameDeclarationNode) jjtGetChild(0);
  }
}
