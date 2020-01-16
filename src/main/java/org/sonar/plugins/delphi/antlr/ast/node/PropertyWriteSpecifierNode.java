package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class PropertyWriteSpecifierNode extends DelphiNode {
  public PropertyWriteSpecifierNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public PrimaryExpressionNode getExpression() {
    return (PrimaryExpressionNode) jjtGetChild(0);
  }
}
