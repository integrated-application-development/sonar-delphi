package org.sonar.plugins.delphi.type;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.type.Type.ClassReferenceType;
import org.sonar.plugins.delphi.type.Type.ScopedType;

public class DelphiClassReferenceType extends DelphiType implements ClassReferenceType, ScopedType {
  private final ScopedType classType;

  private DelphiClassReferenceType(ScopedType classType) {
    super("class of " + classType.getImage());
    this.classType = classType;
  }

  public static ScopedType classOf(ScopedType classType) {
    return new DelphiClassReferenceType(classType);
  }

  @Override
  public boolean isClassReference() {
    return true;
  }

  @Override
  @NotNull
  public DelphiScope typeScope() {
    return classType.typeScope();
  }

  @Override
  public ScopedType classType() {
    return classType;
  }
}
