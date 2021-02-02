package org.sonar.plugins.delphi.type.factory;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.CollectionType;

public class DelphiSetType extends DelphiType implements CollectionType {
  private final Type elementType;

  DelphiSetType(Type elementType) {
    this.elementType = elementType;
  }

  @Override
  public String getImage() {
    return "set of " + elementType().getImage();
  }

  @Override
  public int size() {
    // We're assuming the largest possible size here, but Delphi will actually try to store sets in
    // less bytes if possible.
    // See: https://stackoverflow.com/a/30338451
    return 32;
  }

  @Override
  @NotNull
  public Type elementType() {
    return elementType;
  }

  @Override
  public boolean isSet() {
    return true;
  }
}
