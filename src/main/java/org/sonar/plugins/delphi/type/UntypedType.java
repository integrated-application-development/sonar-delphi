package org.sonar.plugins.delphi.type;

import com.google.errorprone.annotations.Immutable;
import org.sonar.plugins.delphi.type.Type.ImmutableType;

@Immutable
class UntypedType extends DelphiType implements ImmutableType {
  private static final UntypedType INSTANCE = new UntypedType();

  private UntypedType() {
    // Hide constructor
  }

  @Override
  public String getImage() {
    return "<Untyped>";
  }

  @Override
  public boolean isUntyped() {
    return true;
  }

  static ImmutableType instance() {
    return INSTANCE;
  }
}
