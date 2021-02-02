package org.sonar.plugins.delphi.type;

class DelphiVoidType extends DelphiType {
  private static final DelphiVoidType INSTANCE = new DelphiVoidType();

  private DelphiVoidType() {
    // Hide constructor
  }

  @Override
  public String getImage() {
    return "<Void>";
  }

  @Override
  public int size() {
    // meta type
    return 0;
  }

  @Override
  public boolean isVoid() {
    return true;
  }

  static Type instance() {
    return INSTANCE;
  }
}
