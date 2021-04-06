package org.sonar.plugins.delphi.type.factory;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.SubrangeType;

class DelphiSubrangeType extends DelphiType implements SubrangeType {
  private final String image;
  private final Type hostType;

  DelphiSubrangeType(String image, Type hostType) {
    this.image = image;
    this.hostType = hostType;
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public int size() {
    return hostType.size();
  }

  @Override
  @NotNull
  public Type hostType() {
    return hostType;
  }

  @Override
  public boolean isSubrange() {
    return true;
  }
}
