package org.sonar.plugins.delphi.symbol.declaration.parameter;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;

public final class FormalParameter extends AbstractParameter {
  private final String image;
  private final Type type;
  private final boolean hasDefaultValue;
  private final boolean isOut;
  private final boolean isVar;
  private final boolean isConst;

  private FormalParameter(
      String image,
      Type type,
      boolean hasDefaultValue,
      boolean isOut,
      boolean isVar,
      boolean isConst) {
    this.image = image;
    this.type = type;
    this.hasDefaultValue = hasDefaultValue;
    this.isOut = isOut;
    this.isVar = isVar;
    this.isConst = isConst;
  }

  public static Parameter create(FormalParameterData parameter) {
    return new FormalParameter(
        parameter.getImage(),
        parameter.getType(),
        parameter.hasDefaultValue(),
        parameter.isOut(),
        parameter.isVar(),
        parameter.isConst());
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
  }

  @Override
  public boolean hasDefaultValue() {
    return hasDefaultValue;
  }

  @Override
  public boolean isOut() {
    return isOut;
  }

  @Override
  public boolean isVar() {
    return isVar;
  }

  @Override
  public boolean isConst() {
    return isConst;
  }

  @Override
  public Parameter specialize(TypeSpecializationContext context) {
    FormalParameter specialized =
        new FormalParameter(
            image, type.specialize(context), hasDefaultValue, isOut, isVar, isConst);
    if (this.equals(specialized)) {
      return this;
    }
    return specialized;
  }
}
