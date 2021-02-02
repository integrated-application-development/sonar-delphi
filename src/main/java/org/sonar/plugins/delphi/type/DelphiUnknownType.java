package org.sonar.plugins.delphi.type;

class DelphiUnknownType extends DelphiType {
  private static final DelphiUnknownType INSTANCE = new DelphiUnknownType();

  private DelphiUnknownType() {
    // Hide constructor
  }

  @Override
  public String getImage() {
    return "<Unknown>";
  }

  @Override
  public int size() {
    // meta type
    return 0;
  }

  @Override
  public boolean isUnknown() {
    return true;
  }

  static Type instance() {
    return INSTANCE;
  }
}
