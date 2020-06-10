package org.sonar.plugins.delphi.symbol.declaration.parameter;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.symbol.declaration.parameter.Parameter.ImmutableParameter;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ImmutableType;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicMethodData.IntrinsicMethodParameterData;

@Immutable
public class IntrinsicParameter extends AbstractParameter implements ImmutableParameter {
  private final ImmutableType type;
  private final boolean hasDefaultValue;

  private IntrinsicParameter(ImmutableType type, boolean hasDefaultValue) {
    this.type = type;
    this.hasDefaultValue = hasDefaultValue;
  }

  public static ImmutableParameter create(IntrinsicMethodParameterData data) {
    return new IntrinsicParameter(data.getType(), data.hasDefaultValue());
  }

  public static ImmutableParameter create(ImmutableType type) {
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
