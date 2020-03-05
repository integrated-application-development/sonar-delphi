package org.sonar.plugins.delphi.type;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.type.Type.CollectionType;

public class DelphiSetType extends DelphiType implements CollectionType {
  private static final CollectionType EMPTY_SET = new DelphiSetType(DelphiType.voidType());
  private final Type elementType;

  private DelphiSetType(Type elementType) {
    super("set of " + elementType.getImage());
    this.elementType = elementType;
  }

  public static CollectionType set(Type elementType) {
    return new DelphiSetType(elementType);
  }

  public static CollectionType emptySet() {
    return EMPTY_SET;
  }

  @Override
  public boolean isSet() {
    return true;
  }

  @Override
  @NotNull
  public Type elementType() {
    return elementType;
  }
}
