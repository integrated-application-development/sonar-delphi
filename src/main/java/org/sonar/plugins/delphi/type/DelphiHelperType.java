package org.sonar.plugins.delphi.type;

import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.HelperTypeNode;
import org.sonar.plugins.delphi.symbol.DelphiScope;
import org.sonar.plugins.delphi.type.Type.HelperType;

public class DelphiHelperType extends DelphiStructType implements HelperType {
  private final Type helperType;

  private DelphiHelperType(String image, DelphiScope scope, Set<Type> parents, Type helperType) {
    super(image, scope, parents, false);
    this.helperType = helperType;
  }

  public static HelperType from(HelperTypeNode node) {
    return new DelphiHelperType(
        node.jjtGetParent().getImage(),
        node.getScope(),
        node.getParentTypes(),
        node.getFor().getType());
  }

  @Override
  @NotNull
  public Type helperType() {
    return helperType;
  }
}
