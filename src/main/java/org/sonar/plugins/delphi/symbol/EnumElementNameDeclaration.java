package org.sonar.plugins.delphi.symbol;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.EnumElementNode;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public class EnumElementNameDeclaration extends DelphiNameDeclaration implements Typed {
  private final Type type;

  public EnumElementNameDeclaration(EnumElementNode node) {
    super(node.getNameDeclarationNode());
    this.type = node.getType();
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
  }
}
