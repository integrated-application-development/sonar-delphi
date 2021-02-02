package org.sonar.plugins.delphi.symbol.declaration.parameter;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicMethod.IntrinsicParameterData;

public class IntrinsicParameter extends AbstractParameter {
  private final Type type;
  private final boolean hasDefaultValue;

  private IntrinsicParameter(Type type, boolean hasDefaultValue) {
    this.type = type;
    this.hasDefaultValue = hasDefaultValue;
  }

  public static Parameter create(IntrinsicParameterData data) {
    return new IntrinsicParameter(data.getType(), data.hasDefaultValue());
  }

  public static Parameter create(Type type) {
    return new IntrinsicParameter(type, false);
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
}
