package org.sonar.plugins.delphi.symbol.scope;

import static org.sonar.plugins.delphi.symbol.scope.UnknownScope.unknownScope;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.HelperType;
import org.sonar.plugins.delphi.type.Type.ScopedType;
import org.sonar.plugins.delphi.type.Typed;

public class TypeScope extends AbstractDelphiScope implements Typed {
  private final String typeName;
  private Type type;
  private DelphiScope superTypeScope;

  public TypeScope(String typeName) {
    this.typeName = typeName;
    this.type = unknownType();
    this.superTypeScope = unknownScope();
  }

  public TypeScope() {
    this("(anonymous type)");
  }

  public void setType(Type type) {
    this.type = type;

    Type superType = type.superType();
    if (superType instanceof ScopedType) {
      this.superTypeScope = ((ScopedType) superType).typeScope();
    }
  }

  public DelphiScope getSuperTypeScope() {
    return superTypeScope;
  }

  @NotNull
  @Override
  public Type getType() {
    return type;
  }

  @Nullable
  @Override
  protected DelphiScope overloadSearchScope() {
    // A helper type doesn't search for overloads in its ancestors.
    // Instead, it searches for overloads in the extended type.
    // See: https://wiki.freepascal.org/Helper_types#Method_hiding
    if (type.isHelper()) {
      HelperType helperType = ((HelperType) type);
      Type extendedType = helperType.extendedType();
      if (extendedType instanceof ScopedType) {
        return ((ScopedType) extendedType).typeScope();
      }
      return null;
    }
    return superTypeScope;
  }

  @Override
  public String toString() {
    return typeName + " <TypeScope>";
  }
}
