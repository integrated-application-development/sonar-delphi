package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.DelphiProceduralType;
import org.sonar.plugins.delphi.type.Type;

public final class MethodTypeNode extends ProceduralTypeNode {
  public MethodTypeNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  @NotNull
  public Type createType() {
    return DelphiProceduralType.ofObject(getParameterTypes(), getReturnType());
  }
}
