package org.sonar.plugins.delphi.type;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.type.Type.CollectionType;

public abstract class DelphiCollectionType extends DelphiType implements CollectionType {
  private final Type elementType;

  protected DelphiCollectionType(String image, Type elementType) {
    super(image);
    this.elementType = elementType;
  }

  @Override
  @NotNull
  public Type elementType() {
    return elementType;
  }
}
