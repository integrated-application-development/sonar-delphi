package org.sonar.plugins.delphi.symbol.scope;

public class TypeScope extends AbstractDelphiScope {
  private String typeName;

  public TypeScope(String typeName) {
    this.typeName = typeName;
  }

  public TypeScope() {
    this("(anonymous type)");
  }

  @Override
  public String toString() {
    return typeName + " <TypeScope>";
  }
}
