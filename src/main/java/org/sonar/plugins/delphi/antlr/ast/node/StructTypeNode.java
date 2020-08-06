package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.type.DelphiStructType;
import org.sonar.plugins.delphi.type.Type;

public abstract class StructTypeNode extends TypeNode {
  protected StructTypeNode(Token token) {
    super(token);
  }

  @NotNull
  @Override
  public Type createType() {
    return DelphiStructType.from(this);
  }
}
