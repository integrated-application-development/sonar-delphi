package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;

public final class IntegerLiteralNode extends LiteralNode {
  private Type type;

  public IntegerLiteralNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public long getValueAsLong() {
    return parseImage(getImage(), 10);
  }

  @Override
  @NotNull
  public Type getType() {
    if (type == null) {
      type = getTypeFactory().integerFromLiteralValue(this.getValueAsLong());
    }
    return type;
  }
}
