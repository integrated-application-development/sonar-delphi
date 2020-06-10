package org.sonar.plugins.delphi.symbol.declaration.parameter;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.type.Type;

public class PointerMathParameter extends AbstractParameter {
  private final Type type;

  private PointerMathParameter(Type type) {
    this.type = type;
  }

  public static Parameter create(Type type) {
    return new PointerMathParameter(type);
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
  }
}
