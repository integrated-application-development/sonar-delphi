package org.sonar.plugins.delphi.type.factory;

import static org.sonar.plugins.delphi.symbol.scope.DelphiScope.unknownScope;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ClassReferenceType;
import org.sonar.plugins.delphi.type.Type.ScopedType;

class DelphiClassReferenceType extends DelphiType implements ClassReferenceType, ScopedType {
  private Type classType;
  private final int size;

  DelphiClassReferenceType(Type classType, int size) {
    this.classType = classType;
    this.size = size;
  }

  @Override
  public String getImage() {
    return "class of " + classType.getImage();
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isClassReference() {
    return true;
  }

  @Override
  @NotNull
  public DelphiScope typeScope() {
    return classType instanceof ScopedType ? ((ScopedType) classType).typeScope() : unknownScope();
  }

  @Override
  public Type classType() {
    return classType;
  }

  @Override
  public void setClassType(Type type) {
    this.classType = type;
  }
}
