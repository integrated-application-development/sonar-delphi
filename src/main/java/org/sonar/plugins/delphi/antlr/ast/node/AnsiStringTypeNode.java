package org.sonar.plugins.delphi.antlr.ast.node;

import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.ANSISTRING;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;

public final class AnsiStringTypeNode extends TypeNode {
  public AnsiStringTypeNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  @NotNull
  public Type createType() {
    return ANSISTRING.type;
  }
}
