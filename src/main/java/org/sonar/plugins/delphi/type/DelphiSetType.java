package org.sonar.plugins.delphi.type;

public class DelphiSetType extends DelphiCollectionType {
  private static final CollectionType EMPTY_SET = new DelphiSetType(DelphiType.voidType());

  private DelphiSetType(Type elementType) {
    super("set of " + elementType.getImage(), elementType);
  }

  public static CollectionType set(Type elementType) {
    return new DelphiSetType(elementType);
  }

  public static CollectionType emptySet() {
    return EMPTY_SET;
  }

  @Override
  public boolean isSet() {
    return true;
  }
}
