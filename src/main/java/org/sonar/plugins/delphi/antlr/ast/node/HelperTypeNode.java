package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.type.DelphiHelperType;
import org.sonar.plugins.delphi.type.Type;

public abstract class HelperTypeNode extends StructTypeNode {
  public HelperTypeNode(Token token) {
    super(token);
  }

  @NotNull
  public final TypeNode getFor() {
    return getFirstChildOfType(TypeNode.class);
  }

  @NotNull
  @Override
  public final Type createType() {
    return DelphiHelperType.from(this);
  }
}
