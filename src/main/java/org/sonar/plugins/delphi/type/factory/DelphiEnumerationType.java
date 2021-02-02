package org.sonar.plugins.delphi.type.factory;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type.EnumType;

class DelphiEnumerationType extends DelphiType implements EnumType {
  private final String image;
  private final DelphiScope scope;

  DelphiEnumerationType(String image, DelphiScope scope) {
    this.image = image;
    this.scope = scope;
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public int size() {
    // Assumes $MinEnumSize 1 and 256 elements or less.
    // See: https://www.oreilly.com/library/view/delphi-in-a/1565926595/re440.html
    return 1;
  }

  @Override
  @NotNull
  public DelphiScope typeScope() {
    return scope;
  }

  @Override
  public boolean isEnum() {
    return true;
  }
}
