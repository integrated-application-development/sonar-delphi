package org.sonar.plugins.delphi.type;

import static org.sonar.plugins.delphi.symbol.scope.DelphiScope.unknownScope;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.type.Type.ClassReferenceType;
import org.sonar.plugins.delphi.type.Type.ScopedType;

public class DelphiClassReferenceType extends DelphiType implements ClassReferenceType, ScopedType {
  private Type classType;

  private DelphiClassReferenceType(Type classType) {
    this.classType = classType;
  }

  public static ScopedType classOf(Type classType) {
    return new DelphiClassReferenceType(classType);
  }

  @Override
  public String getImage() {
    return "class of " + classType.getImage();
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
