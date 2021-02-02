package org.sonar.plugins.delphi.type;

public class DelphiUnresolvedType extends DelphiType {
  private final String image;

  private DelphiUnresolvedType(String image) {
    this.image = image;
  }

  public static Type referenceTo(String image) {
    return new DelphiUnresolvedType(image);
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public int size() {
    // meta type
    return 0;
  }

  @Override
  public boolean isUnresolved() {
    return true;
  }
}
