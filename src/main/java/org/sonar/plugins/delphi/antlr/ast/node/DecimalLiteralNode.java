package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.DelphiIntrinsicType.DecimalType;
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
    // Technically should be Extended, but in most cases a parameter will probably be Double.
    // This avoids unnecessary work on narrowing the type when trying to match method signatures.
    return DecimalType.DOUBLE.type;
  }
}
