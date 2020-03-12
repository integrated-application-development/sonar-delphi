package org.sonar.plugins.delphi.type;

import com.google.errorprone.annotations.Immutable;
import org.sonar.plugins.delphi.type.Type.ImmutableType;

@Immutable
class UnknownType extends DelphiType implements ImmutableType {
  private static final UnknownType INSTANCE = new UnknownType();

  private UnknownType() {
    super("<Unknown>");
  }

  @Override
  public boolean isUnknown() {
    return true;
  }

  static ImmutableType instance() {
    return INSTANCE;
  }
}
