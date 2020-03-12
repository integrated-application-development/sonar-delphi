package org.sonar.plugins.delphi.type;

import com.google.errorprone.annotations.Immutable;
import org.sonar.plugins.delphi.type.Type.ImmutableType;

@Immutable
class VoidType extends DelphiType implements ImmutableType {
  private static final VoidType INSTANCE = new VoidType();

  private VoidType() {
    super("<Void>");
  }

  @Override
  public boolean isVoid() {
    return true;
  }

  static ImmutableType instance() {
    return INSTANCE;
  }
}
