package org.sonar.plugins.delphi.type;

class DelphiUntypedType extends DelphiType {
  private static final DelphiUntypedType INSTANCE = new DelphiUntypedType();

  private DelphiUntypedType() {
    // Hide constructor
  }

  @Override
  public String getImage() {
    return "<Untyped>";
  }

  @Override
  public int size() {
    // SizeOf returns 0 when the argument is an untyped variable.
    // See: http://docwiki.embarcadero.com/Libraries/en/System.SizeOf
    return 0;
  }

  @Override
  public boolean isUntyped() {
    return true;
  }

  static Type instance() {
    return INSTANCE;
  }
}
