package org.sonar.plugins.delphi.antlr.ast.node;

import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicDecimal.EXTENDED;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;

public final class DecimalLiteralNode extends LiteralNode {
  public DecimalLiteralNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public double getValueAsDouble() {
    return Double.parseDouble(getImage());
  }

  @Override
  @NotNull
  public Type getType() {
    return EXTENDED.type;
  }
}
