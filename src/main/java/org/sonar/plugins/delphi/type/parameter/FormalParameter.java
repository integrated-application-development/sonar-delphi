package org.sonar.plugins.delphi.type.parameter;

import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;

public final class FormalParameter extends AbstractParameter {
  private final String image;

  private FormalParameter(
      String image,
      Type type,
      boolean hasDefaultValue,
      boolean isOut,
      boolean isVar,
      boolean isConst) {
    super(type, hasDefaultValue, isOut, isVar, isConst);
    this.image = image;
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
  public Parameter specialize(TypeSpecializationContext context) {
    FormalParameter specialized =
        new FormalParameter(
            image, getType().specialize(context), hasDefaultValue(), isOut(), isVar(), isConst());
    if (this.equals(specialized)) {
      return this;
    }
    return specialized;
  }
}
