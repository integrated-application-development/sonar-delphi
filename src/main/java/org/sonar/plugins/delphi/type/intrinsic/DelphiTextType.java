package org.sonar.plugins.delphi.type.intrinsic;

import com.google.errorprone.annotations.Immutable;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type.TextType;

@Immutable
class DelphiTextType extends DelphiType implements TextType {

  DelphiTextType(String image) {
    super(image);
  }

  @Override
  public boolean isText() {
    return true;
  }
}
