package org.sonar.plugins.delphi.type.intrinsic;

import com.google.errorprone.annotations.Immutable;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type.TextType;

@Immutable
class DelphiTextType extends DelphiType implements TextType {
  private final String image;

  DelphiTextType(String image) {
    this.image = image;
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public boolean isText() {
    return true;
  }
}
