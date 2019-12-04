package org.sonar.plugins.delphi.type;

import static org.sonar.plugins.delphi.symbol.scope.UnknownScope.unknownScope;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.type.Type.EnumType;
import org.sonar.plugins.delphi.type.Type.ScopedType;

public class DelphiEnumerationType extends DelphiType implements EnumType, ScopedType {
  private final DelphiScope scope;
  @Nullable private final Type baseType;

  private DelphiEnumerationType(String image, DelphiScope scope, @Nullable Type baseType) {
    super(image);
    this.scope = scope;
    this.baseType = baseType;
  }

  public static EnumType enumeration(String image, DelphiScope scope) {
    return new DelphiEnumerationType(image, scope, null);
  }

  public static EnumType subRange(String image, Type baseType) {
    return new DelphiEnumerationType(image, unknownScope(), baseType);
  }

  public static String makeAnonymousImage(String first, String last) {
    return "Enumeration(" + first + ".." + last + ")";
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

  @Override
  @Nullable
  public Type baseType() {
    return baseType;
  }
}
