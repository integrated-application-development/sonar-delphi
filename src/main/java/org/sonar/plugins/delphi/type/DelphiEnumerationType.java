package org.sonar.plugins.delphi.type;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.type.Type.EnumType;

public class DelphiEnumerationType extends DelphiType implements EnumType {
  private final DelphiScope scope;

  private DelphiEnumerationType(String image, DelphiScope scope) {
    super(image);
    this.scope = scope;
  }

  public static EnumType enumeration(String image, DelphiScope scope) {
    return new DelphiEnumerationType(image, scope);
  }

  @Override
  @NotNull
  public DelphiScope typeScope() {
    return scope;
  }

  @Override
  public boolean isEnum() {
    return true;
  }
}
