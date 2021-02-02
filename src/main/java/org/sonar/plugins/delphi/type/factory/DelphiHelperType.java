package org.sonar.plugins.delphi.type.factory;

import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.type.StructKind;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.HelperType;

class DelphiHelperType extends DelphiStructType implements HelperType {
  private final Type extendedType;

  DelphiHelperType(
      List<ImagePart> imageParts,
      int size,
      DelphiScope scope,
      Set<Type> parents,
      Type extendedType,
      StructKind kind) {
    super(imageParts, size, scope, parents, kind);
    this.extendedType = extendedType;
  }

  @Override
  @NotNull
  public Type extendedType() {
    return extendedType;
  }

  @Override
  public boolean isForwardType() {
    return false;
  }

  @Override
  public boolean isHelper() {
    return true;
  }
}
