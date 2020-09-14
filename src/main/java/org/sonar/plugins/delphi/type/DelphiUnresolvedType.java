package org.sonar.plugins.delphi.type;

import com.google.errorprone.annotations.Immutable;
import org.sonar.plugins.delphi.type.Type.ImmutableType;

@Immutable
public class DelphiUnresolvedType extends DelphiType implements ImmutableType {
  private final String image;

  private DelphiUnresolvedType(String image) {
    this.image = image;
  }

  public static ImmutableType referenceTo(String image) {
    return new DelphiUnresolvedType(image);
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public boolean isUnresolved() {
    return true;
  }
}
