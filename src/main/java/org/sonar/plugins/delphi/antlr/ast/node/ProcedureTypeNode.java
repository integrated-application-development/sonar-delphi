package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class ProcedureTypeNode extends TypeNode {
  public ProcedureTypeNode(Token token) {
    super(token);
  }

  public ProcedureTypeNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public String getImage() {
    return ((ProcedureTypeHeadingNode) jjtGetChild(0)).getImage();
  }
}
