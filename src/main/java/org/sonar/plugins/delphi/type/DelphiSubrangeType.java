package org.sonar.plugins.delphi.type;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.type.Type.SubrangeType;

public class DelphiSubrangeType extends DelphiType implements SubrangeType {
  private final String image;
  private final Type hostType;

  private DelphiSubrangeType(String image, Type hostType) {
    this.image = image;
    this.hostType = hostType;
  }

  public static SubrangeType subRange(String image, Type hostType) {
    return new DelphiSubrangeType(image, hostType);
  }

  @Override
  public String getImage() {
    return image;
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
